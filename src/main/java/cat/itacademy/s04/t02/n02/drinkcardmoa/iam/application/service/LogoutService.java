package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.LogoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.LogoutUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenGenerator;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class LogoutService implements LogoutUseCase {

    private static final Logger log = LoggerFactory.getLogger(LogoutService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;

    @Transactional
    public void execute(LogoutCommand command) {
        HashedToken hashedToken = refreshTokenGenerator.hash(command.refreshToken());
        Instant now = Instant.now();

        refreshTokenRepository.findByTokenHash(hashedToken)
                .ifPresent(token -> {
                    refreshTokenRepository.revokeFamily(token.getFamilyId(), now);

                    log.info(
                            "Refresh token family revoked during logout. refreshTokenId={}, familyId={}, userId={}",
                            token.getId().asString(),
                            token.getFamilyId().value(),
                            token.getUserId().asString()
                    );
                });
    }
}
