package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.RefreshTokenCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RefreshTokenResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.RefreshAccessTokenUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenGenerator;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.TokenService;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidTokenException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.RefreshToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenFamilyID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.config.RefreshTokenProperties;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

@Service
@RequiredArgsConstructor
public class RefreshAccessTokenService implements RefreshAccessTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshAccessTokenService.class);

    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RefreshTokenProperties refreshTokenProperties;

    @Override
    @Transactional
    public RefreshTokenResult execute(RefreshTokenCommand cmd) {
        HashedToken hashedToken = refreshTokenGenerator.hash(cmd.rawRefreshToken());

        RefreshTokenAttempt refreshTokenAttempt = Objects.requireNonNull(attemptRefresh(hashedToken));

        if (refreshTokenAttempt.reuseDetected()) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        return refreshTokenAttempt.result();
    }

    private RefreshTokenAttempt attemptRefresh(HashedToken hashedToken) {
        Instant now = Instant.now();
        Instant newExpiresAt = now.plus(refreshTokenProperties.expirationDays(), ChronoUnit.DAYS);

        var currentRefreshToken = refreshTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (currentRefreshToken.wasReplaced()) {
            log.warn("Refresh token reuse attempt detected. refreshTokenId={}", currentRefreshToken.getId().asString());
            return revokeTokenFamily(currentRefreshToken.getFamilyId(), now);
        }

        if (!currentRefreshToken.isUsable(now)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        User user = userRepository.findById(currentRefreshToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        RefreshTokenGenerator.GeneratedRefreshToken generatedRefreshToken = refreshTokenGenerator.generate();

        RefreshToken nextRefreshToken = currentRefreshToken.rotate(generatedRefreshToken.hashedToken(), now, newExpiresAt);

        refreshTokenRepository.save(nextRefreshToken);
        refreshTokenRepository.save(currentRefreshToken);

        String newAccessToken = tokenService.generateToken(user);

        return RefreshTokenAttempt.success(new RefreshTokenResult(
                newAccessToken,
                generatedRefreshToken.rawToken(),
                user.getId().asString(),
                user.getEmail().asString(),
                user.getRole().name()
        ));
    }

    private RefreshTokenAttempt revokeTokenFamily(RefreshTokenFamilyID familyId, Instant now) {
        refreshTokenRepository.revokeFamily(familyId, now);
        return RefreshTokenAttempt.failed();
    }

    private record RefreshTokenAttempt(RefreshTokenResult result, boolean reuseDetected) {

        static RefreshTokenAttempt success(RefreshTokenResult result) {
            return new RefreshTokenAttempt(result, false);
        }

        static RefreshTokenAttempt failed() {
            return new RefreshTokenAttempt(null, true);
        }
    }
}
