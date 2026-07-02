package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.InitiatePasswordResetCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.EventPublisher;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.OpaqueTokenGenerator;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordResetRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.PasswordReset;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.PasswordResetStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.config.PasswordResetProperties;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.ResetPasswordEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitiatePasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetRepository passwordResetRepository;

    @Mock
    private OpaqueTokenGenerator opaqueTokenGenerator;

    @Mock
    private EventPublisher eventPublisher;

    private InitiatePasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new InitiatePasswordResetService(
                userRepository,
                passwordResetRepository,
                opaqueTokenGenerator,
                eventPublisher,
                new PasswordResetProperties(30)
        );
    }

    @Test
    void execute_WhenUserExists_CreatesPasswordResetAndPublishesResetPasswordEvent() {
        InitiatePasswordResetCommand cmd = new InitiatePasswordResetCommand("user@userid.com");

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);
        when(opaqueTokenGenerator.generate()).thenReturn(
                new OpaqueTokenGenerator.GeneratedToken(
                        "raw-password-reset-token",
                        HashedToken.from("hashed-password-reset-token")
                )
        );

        service.execute(cmd);

        ArgumentCaptor<PasswordReset> passwordResetCaptor =
                ArgumentCaptor.forClass(PasswordReset.class);
        verify(passwordResetRepository).save(passwordResetCaptor.capture());
        PasswordReset saved = passwordResetCaptor.getValue();

        ArgumentCaptor<ResetPasswordEvent> eventCaptor =
                ArgumentCaptor.forClass(ResetPasswordEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        ResetPasswordEvent event = eventCaptor.getValue();

        assertAll(
                () -> assertNotNull(saved.getPasswordResetId()),
                () -> assertEquals("user@userid.com", saved.getEmail().asString()),
                () -> assertEquals("hashed-password-reset-token", saved.getToken().asString()),
                () -> assertEquals(PasswordResetStatus.PENDING, saved.getStatus()),
                () -> assertNotNull(saved.getCreatedAt()),
                () -> assertEquals(
                        saved.getCreatedAt().plus(Duration.ofMinutes(30)),
                        saved.getExpiresAt()
                ),
                () -> assertNull(saved.getUsedAt()),
                () -> assertEquals(saved.getPasswordResetId().asString(), event.passwordResetRequestId()),
                () -> assertEquals("user@userid.com", event.email()),
                () -> assertEquals("raw-password-reset-token", event.passwordResetToken()),
                () -> assertEquals(saved.getCreatedAt(), event.occurredOn())
        );
    }

    @Test
    void execute_WhenUserDoesNotExist_DoesNotCreatePasswordResetOrPublishEvent() {
        InitiatePasswordResetCommand cmd = new InitiatePasswordResetCommand("missing@userid.com");

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);

        service.execute(cmd);

        verify(opaqueTokenGenerator, never()).generate();
        verify(passwordResetRepository, never()).save(any(PasswordReset.class));
        verify(eventPublisher, never()).publish(any());
    }
}
