package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.LoginUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.LoginUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordEncoder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenGenerator;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.TokenService;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidCredentialsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.RefreshToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.config.RefreshTokenProperties;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                tokenService,
                userRepository,
                passwordEncoder,
                refreshTokenGenerator,
                refreshTokenRepository,
                new RefreshTokenProperties(
                        30,
                        new RefreshTokenProperties.Cookie(
                                "refresh_token",
                                true,
                                "Lax",
                                "/api/v1/auth"
                        )
                )
        );
    }

    @Test
    void execute_WhenCredentialsAreValid_LoginUser() {

        LoginUserCommand command = new LoginUserCommand(
                "userId@userId.com",
                "12345678"
        );

        User user = createUser();

        when(userRepository.findUserByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(command.password(), user.getHashedPassword().value()))
                .thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("new-token");
        when(refreshTokenGenerator.generate()).thenReturn(
                new RefreshTokenGenerator.GeneratedRefreshToken(
                        "raw-refresh-token",
                        HashedToken.from("hashed-refresh-token")
                )
        );
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant beforeExecution = Instant.now();

        LoginUserResult result = authenticationService.execute(command);

        Instant afterExecution = Instant.now();

        ArgumentCaptor<RefreshToken> refreshTokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        assertNotNull(user);
        assertEquals("new-token", result.token());
        assertEquals("raw-refresh-token", result.refreshToken());
        assertEquals("userid@userid.com", result.email());
        assertEquals(user.getRole().name(), result.role());

        verify(userRepository, times(1)).findUserByEmail(any(Email.class));
        verify(passwordEncoder, times(1))
                .matches(command.password(), user.getHashedPassword().value());
        verify(tokenService, times(1)).generateToken(user);
        verify(refreshTokenGenerator, times(1)).generate();
        verify(refreshTokenRepository, times(1)).save(refreshTokenCaptor.capture());

        RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();

        assertAll(
                () -> assertEquals(user.getId(), savedRefreshToken.getUserId()),
                () -> assertEquals("hashed-refresh-token", savedRefreshToken.getTokenHash().asString()),
                () -> assertNotNull(savedRefreshToken.getId()),
                () -> assertNotNull(savedRefreshToken.getFamilyId()),
                () -> assertNull(savedRefreshToken.getLastUsedAt()),
                () -> assertNull(savedRefreshToken.getRevokedAt()),
                () -> assertNull(savedRefreshToken.getReplacedBy()),
                () -> assertFalse(savedRefreshToken.isRevoked()),
                () -> assertFalse(savedRefreshToken.wasReplaced()),
                () -> assertFalse(savedRefreshToken.getCreatedAt().isBefore(beforeExecution)),
                () -> assertFalse(savedRefreshToken.getCreatedAt().isAfter(afterExecution)),
                () -> assertEquals(
                        savedRefreshToken.getCreatedAt().plus(30, ChronoUnit.DAYS),
                        savedRefreshToken.getExpiresAt()
                )
        );
    }

    @Test
    void execute_WhenEmailDoesNotExist_ThrowInvalidCredentialsException() {
        LoginUserCommand cmd = new LoginUserCommand(
                "missing@userId.com",
                "raw_password"
        );

        when(userRepository.findUserByEmail(any(Email.class)))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.execute(cmd);
        });

        verify(userRepository, times(1)).findUserByEmail(any(Email.class));
        verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
        verify(tokenService, never()).generateToken(any(User.class));
        verify(refreshTokenGenerator, never()).generate();
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void execute_WhenPasswordDoesNotMatch_ThrowInvalidCredentialsException() {
        LoginUserCommand cmd = new LoginUserCommand(
                "user@userId.com",
                "wrong_password"
        );

        User user = createUser();

        when(userRepository.findUserByEmail(any(Email.class)))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(cmd.password(), user.getHashedPassword().value()))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.execute(cmd);
        });

        verify(userRepository, times(1)).findUserByEmail(any(Email.class));
        verify(passwordEncoder, times(1))
                .matches(cmd.password(), user.getHashedPassword().value());
        verify(tokenService, never()).generateToken(any(User.class));
        verify(refreshTokenGenerator, never()).generate();
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void execute_WhenUserIsSuspended_ThrowInvalidCredentialsException() {
        LoginUserCommand cmd = new LoginUserCommand(
                "user@userId.com",
                "12345678"
        );

        User user = createUser(UserStatus.SUSPENDED);

        when(userRepository.findUserByEmail(any(Email.class)))
                .thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.execute(cmd);
        });

        verify(userRepository, times(1)).findUserByEmail(any(Email.class));
        verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
        verify(tokenService, never()).generateToken(any(User.class));
        verify(refreshTokenGenerator, never()).generate();
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void execute_WhenUserIsDeleted_ThrowInvalidCredentialsException() {
        LoginUserCommand cmd = new LoginUserCommand(
                "user@userId.com",
                "12345678"
        );

        User user = createUser(UserStatus.DELETED);

        when(userRepository.findUserByEmail(any(Email.class)))
                .thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.execute(cmd);
        });

        verify(userRepository, times(1)).findUserByEmail(any(Email.class));
        verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
        verify(tokenService, never()).generateToken(any(User.class));
        verify(refreshTokenGenerator, never()).generate();
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    private User createUser() {
        return createUser(UserStatus.ACTIVE);
    }

    private User createUser(UserStatus status) {
        return User.rehydrate(
                VolunteerID.generate(),
                FullName.from("firstName", "lastName"),
                Email.from("userId@userId.com"),
                HashedPassword.from("12345678"),
                Role.VOLUNTEER,
                status
        );
    }
}
