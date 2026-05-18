package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.GetUserByIdQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.GetUserByIdUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.UserNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Service;

@Service
public class GetUserByIdService implements GetUserByIdUseCase {

    private final UserRepository userRepository;

    public GetUserByIdService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserSummaryResult execute(GetUserByIdQuery query) {
        VolunteerID volunteerID = VolunteerID.from(query.userId());
        return userRepository.findById(volunteerID)
                .map(UserSummaryResult::from)
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with id: " + query.userId()));
    }
}
