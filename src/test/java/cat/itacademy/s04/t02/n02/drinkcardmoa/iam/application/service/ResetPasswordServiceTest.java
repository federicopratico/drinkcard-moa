package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.ResetPasswordCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.OpaqueTokenGenerator;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordEncoder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordResetRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidTokenException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.PasswordReset;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.PasswordResetStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.PasswordResetID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

    private static final String RAW_PASSWORD_RESET_TOKEN = "raw-password-reset-token";
    private static final String NEW_PASSWORD = "new-password";
    private static final String ENCODED_NEW_PASSWORD = "encoded-new-password";
    private static final Email EMAIL = Email.from("user@email.com");
    private static final HashedToken HASHED_PASSWORD_RESET_TOKEN =
            HashedToken.from("hashed-password-reset-token");

    @Mock
    private UserRepository userRepository;

    @Mock
    private OpaqueTokenGenerator opaqueTokenGenerator;

    @Mock
    private PasswordResetRepository passwordResetRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ResetPasswordService service;

    @BeforeEach
    void setUp() {
        service = new ResetPasswordService(
                userRepository,
                opaqueTokenGenerator,
                passwordResetRepository,
                passwordEncoder
        );
    }

    @Test
    void execute_WhenPasswordResetTokenIsValid_ShouldChangePasswordMarkRequestAsUsedAndRevokeOtherPendingRequests() {
        PasswordReset passwordReset = activePasswordReset();
        User user = activeUser();

        when(opaqueTokenGenerator.hash(RAW_PASSWORD_RESET_TOKEN))
                .thenReturn(HASHED_PASSWORD_RESET_TOKEN);
        when(passwordResetRepository.findByPasswordResetToken(HASHED_PASSWORD_RESET_TOKEN))
                .thenReturn(Optional.of(passwordReset));
        when(userRepository.findUserByEmail(EMAIL))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode(NEW_PASSWORD))
                .thenReturn(ENCODED_NEW_PASSWORD);

        Instant beforeExecution = Instant.now();

        service.execute(new ResetPasswordCommand(RAW_PASSWORD_RESET_TOKEN, NEW_PASSWORD));

        Instant afterExecution = Instant.now();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<PasswordReset> passwordResetCaptor =
                ArgumentCaptor.forClass(PasswordReset.class);
        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        ArgumentCaptor<PasswordResetID> passwordResetIdCaptor =
                ArgumentCaptor.forClass(PasswordResetID.class);

        verify(userRepository).save(userCaptor.capture());
        verify(passwordResetRepository).save(passwordResetCaptor.capture());
        verify(passwordResetRepository).revokePendingByEmailExceptCurrent(
                emailCaptor.capture(),
                passwordResetIdCaptor.capture()
        );

        User savedUser = userCaptor.getValue();
        PasswordReset savedPasswordReset = passwordResetCaptor.getValue();

        assertAll(
                () -> assertEquals(ENCODED_NEW_PASSWORD, savedUser.getHashedPassword().value()),
                () -> assertEquals(PasswordResetStatus.USED, savedPasswordReset.getStatus()),
                () -> assertNotNull(savedPasswordReset.getUsedAt()),
                () -> assertFalse(savedPasswordReset.getUsedAt().isBefore(beforeExecution)),
                () -> assertFalse(savedPasswordReset.getUsedAt().isAfter(afterExecution)),
                () -> assertEquals(EMAIL, emailCaptor.getValue()),
                () -> assertEquals(passwordReset.getPasswordResetId(), passwordResetIdCaptor.getValue())
        );

        InOrder inOrder = inOrder(userRepository, passwordResetRepository);
        inOrder.verify(userRepository).save(user);
        inOrder.verify(passwordResetRepository).save(passwordReset);
        inOrder.verify(passwordResetRepository).revokePendingByEmailExceptCurrent(
                passwordReset.getEmail(),
                passwordReset.getPasswordResetId()
        );
    }

    @Test
    void execute_WhenPasswordResetTokenDoesNotExist_ShouldThrowInvalidTokenException() {
        when(opaqueTokenGenerator.hash(RAW_PASSWORD_RESET_TOKEN))
                .thenReturn(HASHED_PASSWORD_RESET_TOKEN);
        when(passwordResetRepository.findByPasswordResetToken(HASHED_PASSWORD_RESET_TOKEN))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidTokenException.class,
                () -> service.execute(new ResetPasswordCommand(RAW_PASSWORD_RESET_TOKEN, NEW_PASSWORD))
        );

        verify(userRepository, never()).findUserByEmail(any(Email.class));
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordResetRepository, never()).save(any(PasswordReset.class));
        verify(passwordResetRepository, never()).revokePendingByEmailExceptCurrent(any(), any());
    }

    @Test
    void execute_WhenPasswordResetTokenIsExpired_ShouldThrowInvalidTokenException() {
        PasswordReset expiredPasswordReset = expiredPasswordReset();

        when(opaqueTokenGenerator.hash(RAW_PASSWORD_RESET_TOKEN))
                .thenReturn(HASHED_PASSWORD_RESET_TOKEN);
        when(passwordResetRepository.findByPasswordResetToken(HASHED_PASSWORD_RESET_TOKEN))
                .thenReturn(Optional.of(expiredPasswordReset));

        assertThrows(
                InvalidTokenException.class,
                () -> service.execute(new ResetPasswordCommand(RAW_PASSWORD_RESET_TOKEN, NEW_PASSWORD))
        );

        verify(userRepository, never()).findUserByEmail(any(Email.class));
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordResetRepository, never()).save(any(PasswordReset.class));
        verify(passwordResetRepository, never()).revokePendingByEmailExceptCurrent(any(), any());
    }

    @Test
    void execute_WhenPasswordResetTokenWasAlreadyUsed_ShouldThrowInvalidTokenException() {
        PasswordReset usedPasswordReset = usedPasswordReset();

        when(opaqueTokenGenerator.hash(RAW_PASSWORD_RESET_TOKEN))
                .thenReturn(HASHED_PASSWORD_RESET_TOKEN);
        when(passwordResetRepository.findByPasswordResetToken(HASHED_PASSWORD_RESET_TOKEN))
                .thenReturn(Optional.of(usedPasswordReset));

        assertThrows(
                InvalidTokenException.class,
                () -> service.execute(new ResetPasswordCommand(RAW_PASSWORD_RESET_TOKEN, NEW_PASSWORD))
        );

        verify(userRepository, never()).findUserByEmail(any(Email.class));
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordResetRepository, never()).save(any(PasswordReset.class));
        verify(passwordResetRepository, never()).revokePendingByEmailExceptCurrent(any(), any());
    }

    @Test
    void execute_WhenUserDoesNotExist_ShouldThrowInvalidTokenException() {
        PasswordReset passwordReset = activePasswordReset();

        when(opaqueTokenGenerator.hash(RAW_PASSWORD_RESET_TOKEN))
                .thenReturn(HASHED_PASSWORD_RESET_TOKEN);
        when(passwordResetRepository.findByPasswordResetToken(HASHED_PASSWORD_RESET_TOKEN))
                .thenReturn(Optional.of(passwordReset));
        when(userRepository.findUserByEmail(EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidTokenException.class,
                () -> service.execute(new ResetPasswordCommand(RAW_PASSWORD_RESET_TOKEN, NEW_PASSWORD))
        );

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordResetRepository, never()).save(any(PasswordReset.class));
        verify(passwordResetRepository, never()).revokePendingByEmailExceptCurrent(any(), any());
    }

    private PasswordReset activePasswordReset() {
        return PasswordReset.create(
                PasswordResetID.generate(),
                EMAIL,
                HASHED_PASSWORD_RESET_TOKEN,
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(3600)
        );
    }

    private PasswordReset expiredPasswordReset() {
        return PasswordReset.create(
                PasswordResetID.generate(),
                EMAIL,
                HASHED_PASSWORD_RESET_TOKEN,
                Instant.now().minusSeconds(7200),
                Instant.now().minusSeconds(3600)
        );
    }

    private PasswordReset usedPasswordReset() {
        Instant usedAt = Instant.now().minusSeconds(60);

        return PasswordReset.rehydrate(
                PasswordResetID.generate(),
                EMAIL,
                HASHED_PASSWORD_RESET_TOKEN,
                PasswordResetStatus.USED,
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600),
                usedAt
        );
    }

    private User activeUser() {
        return User.rehydrate(
                VolunteerID.generate(),
                FullName.from("First", "Last"),
                EMAIL,
                HashedPassword.from("old-hashed-password"),
                Role.VOLUNTEER,
                UserStatus.ACTIVE
        );
    }
}
