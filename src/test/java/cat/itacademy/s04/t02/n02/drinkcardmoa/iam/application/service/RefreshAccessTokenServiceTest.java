package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.RefreshTokenCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RefreshTokenResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenGenerator;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.TokenService;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidTokenException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.RefreshToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenFamilyID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.config.RefreshTokenProperties;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshAccessTokenServiceTest {

    private static final String RAW_REFRESH_TOKEN = "raw-refresh-token";
    private static final HashedToken HASHED_REFRESH_TOKEN =
            HashedToken.from("hashed-refresh-token");

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    private RefreshAccessTokenService service;

    @BeforeEach
    void setUp() {
        RefreshTokenProperties refreshTokenProperties = new RefreshTokenProperties(
                30,
                new RefreshTokenProperties.Cookie(
                        "refresh_token",
                        true,
                        "Lax",
                        "/api/v1/auth"
                )
        );

        service = new RefreshAccessTokenService(
                refreshTokenGenerator,
                refreshTokenRepository,
                userRepository,
                tokenService,
                refreshTokenProperties
        );
    }

    @Test
    void execute_WhenRefreshTokenIsValid_ShouldRotateTokenAndReturnNewSession() {
        RefreshToken currentRefreshToken = activeRefreshToken();
        User user = activeUser(currentRefreshToken.getUserId());

        when(refreshTokenGenerator.hash(RAW_REFRESH_TOKEN))
                .thenReturn(HASHED_REFRESH_TOKEN);
        when(refreshTokenRepository.findByTokenHash(HASHED_REFRESH_TOKEN))
                .thenReturn(Optional.of(currentRefreshToken));
        when(userRepository.findById(currentRefreshToken.getUserId()))
                .thenReturn(Optional.of(user));
        when(refreshTokenGenerator.generate()).thenReturn(
                new RefreshTokenGenerator.GeneratedRefreshToken(
                        "new-raw-refresh-token",
                        HashedToken.from("new-hashed-refresh-token")
                )
        );
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenService.generateToken(user)).thenReturn("new-access-token");

        Instant beforeExecution = Instant.now();

        RefreshTokenResult result =
                service.execute(new RefreshTokenCommand(RAW_REFRESH_TOKEN));

        Instant afterExecution = Instant.now();

        ArgumentCaptor<RefreshToken> refreshTokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository, times(2)).save(refreshTokenCaptor.capture());

        RefreshToken nextRefreshToken = refreshTokenCaptor.getAllValues().get(0);
        RefreshToken savedCurrentRefreshToken = refreshTokenCaptor.getAllValues().get(1);

        assertAll(
                () -> assertEquals("new-access-token", result.accessToken()),
                () -> assertEquals("new-raw-refresh-token", result.refreshToken()),
                () -> assertEquals(user.getId().asString(), result.volunteerId()),
                () -> assertEquals(user.getEmail().asString(), result.email()),
                () -> assertEquals(user.getRole().name(), result.role()),

                () -> assertNotEquals(currentRefreshToken.getId(), nextRefreshToken.getId()),
                () -> assertEquals(currentRefreshToken.getUserId(), nextRefreshToken.getUserId()),
                () -> assertEquals(currentRefreshToken.getFamilyId(), nextRefreshToken.getFamilyId()),
                () -> assertEquals("new-hashed-refresh-token", nextRefreshToken.getTokenHash().asString()),
                () -> assertFalse(nextRefreshToken.isRevoked()),
                () -> assertFalse(nextRefreshToken.wasReplaced()),
                () -> assertFalse(nextRefreshToken.getCreatedAt().isBefore(beforeExecution)),
                () -> assertFalse(nextRefreshToken.getCreatedAt().isAfter(afterExecution)),
                () -> assertEquals(
                        nextRefreshToken.getCreatedAt().plus(30, ChronoUnit.DAYS),
                        nextRefreshToken.getExpiresAt()
                ),

                () -> assertEquals(currentRefreshToken.getId(), savedCurrentRefreshToken.getId()),
                () -> assertTrue(savedCurrentRefreshToken.isRevoked()),
                () -> assertTrue(savedCurrentRefreshToken.wasReplaced()),
                () -> assertEquals(nextRefreshToken.getId(), savedCurrentRefreshToken.getReplacedBy()),
                () -> assertEquals(savedCurrentRefreshToken.getLastUsedAt(), savedCurrentRefreshToken.getRevokedAt())
        );

        InOrder inOrder = inOrder(refreshTokenRepository);
        inOrder.verify(refreshTokenRepository).save(nextRefreshToken);
        inOrder.verify(refreshTokenRepository).save(savedCurrentRefreshToken);
    }

    @Test
    void execute_WhenRefreshTokenDoesNotExist_ShouldThrowInvalidTokenExceptionAndReleaseLock() {
        when(refreshTokenGenerator.hash(RAW_REFRESH_TOKEN))
                .thenReturn(HASHED_REFRESH_TOKEN);
        when(refreshTokenRepository.findByTokenHash(HASHED_REFRESH_TOKEN))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidTokenException.class,
                () -> service.execute(new RefreshTokenCommand(RAW_REFRESH_TOKEN))
        );

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).revokeFamily(any(), any());
    }

    @Test
    void execute_WhenRefreshTokenIsExpired_ShouldThrowInvalidTokenExceptionAndReleaseLock() {
        RefreshToken expiredToken = expiredRefreshToken();

        when(refreshTokenGenerator.hash(RAW_REFRESH_TOKEN))
                .thenReturn(HASHED_REFRESH_TOKEN);
        when(refreshTokenRepository.findByTokenHash(HASHED_REFRESH_TOKEN))
                .thenReturn(Optional.of(expiredToken));

        assertThrows(
                InvalidTokenException.class,
                () -> service.execute(new RefreshTokenCommand(RAW_REFRESH_TOKEN))
        );

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).revokeFamily(any(), any());
    }

    @Test
    void execute_WhenRefreshTokenWasAlreadyReplaced_ShouldRevokeFamilyThenThrowInvalidTokenException() {
        RefreshToken replacedToken = replacedRefreshToken();

        when(refreshTokenGenerator.hash(RAW_REFRESH_TOKEN))
                .thenReturn(HASHED_REFRESH_TOKEN);
        when(refreshTokenRepository.findByTokenHash(HASHED_REFRESH_TOKEN))
                .thenReturn(Optional.of(replacedToken));

        assertThrows(
                InvalidTokenException.class,
                () -> service.execute(new RefreshTokenCommand(RAW_REFRESH_TOKEN))
        );

        verify(refreshTokenRepository).revokeFamily(
                any(RefreshTokenFamilyID.class),
                any(Instant.class)
        );
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void execute_WhenUserDoesNotExist_ShouldThrowInvalidTokenExceptionAndReleaseLock() {
        RefreshToken currentRefreshToken = activeRefreshToken();

        when(refreshTokenGenerator.hash(RAW_REFRESH_TOKEN))
                .thenReturn(HASHED_REFRESH_TOKEN);
        when(refreshTokenRepository.findByTokenHash(HASHED_REFRESH_TOKEN))
                .thenReturn(Optional.of(currentRefreshToken));
        when(userRepository.findById(currentRefreshToken.getUserId()))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidTokenException.class,
                () -> service.execute(new RefreshTokenCommand(RAW_REFRESH_TOKEN))
        );

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).revokeFamily(any(), any());
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

    private RefreshToken expiredRefreshToken() {
        Instant createdAt = Instant.now().minusSeconds(7200);
        Instant expiresAt = Instant.now().minusSeconds(3600);

        return RefreshToken.create(
                RefreshTokenID.generate(),
                VolunteerID.generate(),
                HASHED_REFRESH_TOKEN,
                RefreshTokenFamilyID.generate(),
                createdAt,
                expiresAt
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

    private User activeUser(VolunteerID volunteerId) {
        return User.rehydrate(
                volunteerId,
                FullName.from("First", "Last"),
                Email.from("user@email.com"),
                HashedPassword.from("hashed-password"),
                Role.VOLUNTEER,
                UserStatus.ACTIVE
        );
    }
}
