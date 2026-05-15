package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.LoginUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.LoginUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.AuthenticateUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordEncoder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.TokenService;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidCredentialsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements AuthenticateUserUseCase {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(TokenService tokenService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginUserResult execute(LoginUserCommand cmd) {

        User user = userRepository.findUserByEmail(Email.from(cmd.email()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if(!passwordEncoder.matches(cmd.password(), user.getHashedPassword().value()))
            throw new InvalidCredentialsException("Invalid email or password");

        String token = tokenService.generateToken(user);

        return LoginUserResult.from(user, token);
    }
}
