package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.LogoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.OpaqueTokenGenerator;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.RefreshToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenFamilyID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    private static final String RAW_REFRESH_TOKEN = "raw-refresh-token";
    private static final HashedToken HASHED_REFRESH_TOKEN =
            HashedToken.from("hashed-refresh-token");

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private OpaqueTokenGenerator opaqueTokenGenerator;

    private LogoutService service;

    @BeforeEach
    void setUp() {
        service = new LogoutService(refreshTokenRepository, opaqueTokenGenerator);
    }

    @Test
    void execute_WhenRefreshTokenExists_ShouldRevokeTokenFamily() {
        RefreshToken refreshToken = activeRefreshToken();

        when(opaqueTokenGenerator.hash(RAW_REFRESH_TOKEN))
                .thenReturn(HASHED_REFRESH_TOKEN);
        when(refreshTokenRepository.findByTokenHash(HASHED_REFRESH_TOKEN))
                .thenReturn(Optional.of(refreshToken));

        Instant beforeExecution = Instant.now();

        service.execute(new LogoutCommand(RAW_REFRESH_TOKEN));

        Instant afterExecution = Instant.now();

        ArgumentCaptor<RefreshTokenFamilyID> familyIdCaptor =
                ArgumentCaptor.forClass(RefreshTokenFamilyID.class);
        ArgumentCaptor<Instant> revokedAtCaptor =
                ArgumentCaptor.forClass(Instant.class);

        verify(refreshTokenRepository).revokeFamily(
                familyIdCaptor.capture(),
                revokedAtCaptor.capture()
        );

        assertAll(
                () -> assertEquals(refreshToken.getFamilyId(), familyIdCaptor.getValue()),
                () -> assertFalse(revokedAtCaptor.getValue().isBefore(beforeExecution)),
                () -> assertFalse(revokedAtCaptor.getValue().isAfter(afterExecution))
        );
    }

    @Test
    void execute_WhenRefreshTokenDoesNotExist_ShouldDoNothing() {
        when(opaqueTokenGenerator.hash(RAW_REFRESH_TOKEN))
                .thenReturn(HASHED_REFRESH_TOKEN);
        when(refreshTokenRepository.findByTokenHash(HASHED_REFRESH_TOKEN))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.execute(new LogoutCommand(RAW_REFRESH_TOKEN)));

        verify(refreshTokenRepository, never()).revokeFamily(any(), any());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void execute_WhenRefreshTokenIsAlreadyRevoked_ShouldRemainIdempotentAndRevokeFamily() {
        RefreshToken refreshToken = revokedRefreshToken();

        when(opaqueTokenGenerator.hash(RAW_REFRESH_TOKEN))
                .thenReturn(HASHED_REFRESH_TOKEN);
        when(refreshTokenRepository.findByTokenHash(HASHED_REFRESH_TOKEN))
                .thenReturn(Optional.of(refreshToken));

        assertDoesNotThrow(() -> service.execute(new LogoutCommand(RAW_REFRESH_TOKEN)));

        verify(refreshTokenRepository).revokeFamily(
                eq(refreshToken.getFamilyId()),
                any(Instant.class)
        );
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void execute_WhenRefreshTokenWasAlreadyReplaced_ShouldRevokeTokenFamily() {
        RefreshToken refreshToken = replacedRefreshToken();

        when(opaqueTokenGenerator.hash(RAW_REFRESH_TOKEN))
                .thenReturn(HASHED_REFRESH_TOKEN);
        when(refreshTokenRepository.findByTokenHash(HASHED_REFRESH_TOKEN))
                .thenReturn(Optional.of(refreshToken));

        assertDoesNotThrow(() -> service.execute(new LogoutCommand(RAW_REFRESH_TOKEN)));

        verify(refreshTokenRepository).revokeFamily(
                eq(refreshToken.getFamilyId()),
                any(Instant.class)
        );
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    private RefreshToken activeRefreshToken() {
        Instant createdAt = Instant.now().minusSeconds(60);
        Instant expiresAt = Instant.now().plusSeconds(3600);

        return RefreshToken.create(
                RefreshTokenID.generate(),
                VolunteerID.generate(),
                HASHED_REFRESH_TOKEN,
                RefreshTokenFamilyID.generate(),
                createdAt,
                expiresAt
        );
    }

    private RefreshToken revokedRefreshToken() {
        Instant createdAt = Instant.now().minusSeconds(120);
        Instant expiresAt = Instant.now().plusSeconds(3600);
        Instant revokedAt = Instant.now().minusSeconds(60);

        return RefreshToken.rehydrate(
                RefreshTokenID.generate(),
                VolunteerID.generate(),
                HASHED_REFRESH_TOKEN,
                RefreshTokenFamilyID.generate(),
                createdAt,
                expiresAt,
                null,
                revokedAt,
                null
        );
    }

    private RefreshToken replacedRefreshToken() {
        Instant createdAt = Instant.now().minusSeconds(120);
        Instant expiresAt = Instant.now().plusSeconds(3600);
        Instant replacedAt = Instant.now().minusSeconds(60);

        return RefreshToken.rehydrate(
                RefreshTokenID.generate(),
                VolunteerID.generate(),
                HASHED_REFRESH_TOKEN,
                RefreshTokenFamilyID.generate(),
                createdAt,
                expiresAt,
                replacedAt,
                replacedAt,
                RefreshTokenID.generate()
        );
    }
}
