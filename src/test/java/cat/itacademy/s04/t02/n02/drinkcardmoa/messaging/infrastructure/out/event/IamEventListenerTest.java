package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.infrastructure.out.event;

import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command.SendInvitationEmailCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command.SendResetPasswordEmailCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.usecase.SendInvitationEmailUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.usecase.SendPasswordResetEmailUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.ResetPasswordEvent;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.UserInvitedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IamEventListenerTest {

    @Mock
    private SendInvitationEmailUseCase sendInvitationEmailUseCase;

    @Mock
    private SendPasswordResetEmailUseCase sendPasswordResetEmailUseCase;

    @InjectMocks
    private IamEventListener iamEventListener;

    @Test
    void onUserInvited_WhenEventIsValid_DispatchesSendInvitationEmailCommand() {
        UserInvitedEvent event = new UserInvitedEvent(
                "invitation-id",
                "invitee@userid.com",
                "VOLUNTEER",
                "token-abc",
                Instant.now()
        );

        when(sendInvitationEmailUseCase.execute(any(SendInvitationEmailCommand.class)))
                .thenReturn(true);

        iamEventListener.onUserInvited(event);

        ArgumentCaptor<SendInvitationEmailCommand> commandCaptor =
                ArgumentCaptor.forClass(SendInvitationEmailCommand.class);

        verify(sendInvitationEmailUseCase, times(1)).execute(commandCaptor.capture());

        SendInvitationEmailCommand cmd = commandCaptor.getValue();

        assertAll(
                () -> assertEquals(event.email(), cmd.email()),
                () -> assertEquals(event.role(), cmd.role()),
                () -> assertEquals(event.invitationToken(), cmd.invitationToken())
        );
    }

    @Test
    void onUserInvited_WhenUseCaseFails_RethrowsException() {
        UserInvitedEvent event = new UserInvitedEvent(
                "invitation-id",
                "invitee@userid.com",
                "VOLUNTEER",
                "token-abc",
                Instant.now()
        );

        RuntimeException exception = new RuntimeException("Mailtrap unavailable");
        when(sendInvitationEmailUseCase.execute(any(SendInvitationEmailCommand.class)))
                .thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> iamEventListener.onUserInvited(event)
        );

        assertEquals(exception, thrown);
        verify(sendInvitationEmailUseCase, times(1)).execute(any(SendInvitationEmailCommand.class));
    }

    @Test
    void onResetPassword_WhenEventIsValid_DispatchesSendResetPasswordEmailCommand() {
        ResetPasswordEvent event = new ResetPasswordEvent(
                "reset-password-id",
                "user@userid.com",
                "reset-token-abc",
                Instant.now()
        );

        when(sendPasswordResetEmailUseCase.execute(any(SendResetPasswordEmailCommand.class)))
                .thenReturn(true);

        iamEventListener.onResetPassword(event);

        ArgumentCaptor<SendResetPasswordEmailCommand> commandCaptor =
                ArgumentCaptor.forClass(SendResetPasswordEmailCommand.class);

        verify(sendPasswordResetEmailUseCase, times(1)).execute(commandCaptor.capture());

        SendResetPasswordEmailCommand cmd = commandCaptor.getValue();

        assertAll(
                () -> assertEquals(event.email(), cmd.email()),
                () -> assertEquals(event.passwordResetToken(), cmd.passwordResetToken())
        );
    }

    @Test
    void onResetPassword_WhenUseCaseFails_RethrowsException() {
        ResetPasswordEvent event = new ResetPasswordEvent(
                "reset-password-id",
                "user@userid.com",
                "reset-token-abc",
                Instant.now()
        );

        RuntimeException exception = new RuntimeException("Mailtrap unavailable");
        when(sendPasswordResetEmailUseCase.execute(any(SendResetPasswordEmailCommand.class)))
                .thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> iamEventListener.onResetPassword(event)
        );

        assertEquals(exception, thrown);
        verify(sendPasswordResetEmailUseCase, times(1)).execute(any(SendResetPasswordEmailCommand.class));
    }
}
