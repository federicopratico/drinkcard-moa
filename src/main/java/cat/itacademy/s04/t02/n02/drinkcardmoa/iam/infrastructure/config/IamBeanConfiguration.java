package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.config;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.AuthenticateUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.RegisterUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordEncoder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.TokenService;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service.AuthenticationService;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service.RegisterUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IamBeanConfiguration {

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserRepository userRepository, PasswordEncoder passwordEncoder, EventPublisher eventPublisher) {
        return new RegisterUserService(userRepository, passwordEncoder, eventPublisher);
    }

    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        return new AuthenticationService(tokenService, userRepository, passwordEncoder);
    }
}
