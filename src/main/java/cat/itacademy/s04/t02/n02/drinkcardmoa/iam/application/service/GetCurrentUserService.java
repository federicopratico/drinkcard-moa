package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.CurrentUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.CurrentUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.GetCurrentUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.UserNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import org.springframework.stereotype.Service;

@Service
public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final UserRepository userRepository;

    public GetCurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CurrentUserResult execute(CurrentUserCommand cmd) {
        User user = userRepository.findUserByEmail(Email.from(cmd.email()))
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + cmd.email()));

        return CurrentUserResult.from(user);
    }
}
