package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.infrastructure.out.event;

import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command.SendInvitationEmailCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.usecase.SendInvitationEmailUseCase;
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
}
