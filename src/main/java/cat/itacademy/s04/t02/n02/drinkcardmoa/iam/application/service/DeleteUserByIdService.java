package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.DeleteUserByIdQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.GetUserByIdQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.DeleteUserByIdUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.GetUserByIdUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.CannotDeleteAdminException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.UserConflictException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.UserNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Service;

import static cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role.ADMIN;

@Service
public class DeleteUserByIdService implements DeleteUserByIdUseCase {

    private final UserRepository userRepository;

    public DeleteUserByIdService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void execute(DeleteUserByIdQuery query) {
        if (query.userId().equals(query.deletedBy())) {
            throw new UserConflictException("A user cannot delete themselves.");
        }

        VolunteerID volunteerID = VolunteerID.from(query.userId());
        User user = userRepository.findById(volunteerID)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + volunteerID.value() + " not found"));

        if (user.getRole().equals(ADMIN)) {
            throw new CannotDeleteAdminException("Cannot delete an admin user.");
        }

        userRepository.delete(user);
    }
}
