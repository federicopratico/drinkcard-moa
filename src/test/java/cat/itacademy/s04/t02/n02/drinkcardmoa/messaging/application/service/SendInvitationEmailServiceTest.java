package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command.SendInvitationEmailCommand;
import io.mailtrap.model.response.emails.SendResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendInvitationEmailServiceTest {

    @Mock
    private MailTrapService mailTrapService;

    private final URI registrationLinkBaseUrl = URI.create("https://example.com/register");
    private final Map<EmailType, String> templates = Map.of(
            EmailType.INVITATION, "147f6471-ece8-42f1-8f7a-509ac560dda9"
    );

    private SendInvitationEmailService sendInvitationEmailService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        sendInvitationEmailService = new SendInvitationEmailService(mailTrapService, registrationLinkBaseUrl, templates);
    }

    @Test
    void execute_WhenMailtrapSucceeds_ReturnsTrueAndSendsInvitationEmail() {
        SendInvitationEmailCommand cmd = new SendInvitationEmailCommand(
                "invitee@userid.com",
                "VOLUNTEER",
                "token-123"
        );

        SendResponse response = new SendResponse();
        response.setSuccess(true);
        when(mailTrapService.sendEmail(anyString(), anyString(), anyMap()))
                .thenReturn(response);

        boolean result = sendInvitationEmailService.execute(cmd);

        ArgumentCaptor<String> recipientCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);

        verify(mailTrapService).sendEmail(
                recipientCaptor.capture(),
                templateCaptor.capture(),
                variablesCaptor.capture()
        );

        Map<String, Object> variables = variablesCaptor.getValue();

        assertAll(
                () -> assertTrue(result),
                () -> assertEquals("invitee@userid.com", recipientCaptor.getValue()),
                () -> assertEquals("147f6471-ece8-42f1-8f7a-509ac560dda9", templateCaptor.getValue()),
                () -> assertEquals("invitee@userid.com", variables.get("email")),
                () -> assertEquals("VOLUNTEER", variables.get("role")),
                () -> assertEquals(
                        "https://example.com/register?invitation_token=token-123",
                        variables.get("registration_link")),
                () -> assertEquals(3, variables.size())
        );
    }

    @Test
    void execute_WhenMailtrapFails_ReturnsFalse() {
        SendInvitationEmailCommand cmd = new SendInvitationEmailCommand(
                "invitee@userid.com",
                "VOLUNTEER",
                "token-123"
        );

        SendResponse response = new SendResponse();
        response.setSuccess(false);
        when(mailTrapService.sendEmail(anyString(), anyString(), anyMap()))
                .thenReturn(response);

        boolean result = sendInvitationEmailService.execute(cmd);

        assertFalse(result);
    }

    @Test
    void execute_WhenMailtrapThrows_PropagatesException() {
        SendInvitationEmailCommand cmd = new SendInvitationEmailCommand(
                "invitee@userid.com",
                "VOLUNTEER",
                "token-123"
        );

        RuntimeException boom = new RuntimeException("mailtrap down");
        when(mailTrapService.sendEmail(anyString(), anyString(), anyMap()))
                .thenThrow(boom);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> sendInvitationEmailService.execute(cmd)
        );
        assertEquals(boom, thrown);
    }
}
