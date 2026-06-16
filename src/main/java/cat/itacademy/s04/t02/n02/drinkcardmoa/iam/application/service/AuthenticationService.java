package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.LoginUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.LoginUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.AuthenticateUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.*;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidCredentialsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.RefreshToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenFamilyID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.config.RefreshTokenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
@Service
public class AuthenticationService implements AuthenticateUserUseCase {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenProperties refreshTokenProperties;

    @Transactional
    @Override
    public LoginUserResult execute(LoginUserCommand cmd) {

        User user = userRepository.findUserByEmail(Email.from(cmd.email()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if(!user.isActive()) throw new InvalidCredentialsException("Invalid Credentials.\nPlease contact the administrator.");

        if(!passwordEncoder.matches(cmd.password(), user.getHashedPassword().value()))
            throw new InvalidCredentialsException("Invalid email or password");

        String token = tokenService.generateToken(user);

        RefreshTokenGenerator.GeneratedRefreshToken generatedRefreshToken = refreshTokenGenerator.generate();

        Instant now = Instant.now();
        Instant expiresAt = now.plus(refreshTokenProperties.expirationDays(), ChronoUnit.DAYS);

        RefreshToken refreshToken = RefreshToken.create(
                RefreshTokenID.generate(),
                user.getId(),
                generatedRefreshToken.hashedToken(),
                RefreshTokenFamilyID.generate(),
                now,
                expiresAt
        );

        refreshTokenRepository.save(refreshToken);

        return LoginUserResult.from(user, token, generatedRefreshToken.rawToken());
    }
}
