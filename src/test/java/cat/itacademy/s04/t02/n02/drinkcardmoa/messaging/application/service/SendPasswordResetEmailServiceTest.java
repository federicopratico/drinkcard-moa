package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command.SendResetPasswordEmailCommand;
import io.mailtrap.model.response.emails.SendResponse;
import org.junit.jupiter.api.BeforeEach;
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
class SendPasswordResetEmailServiceTest {

    @Mock
    private MailTrapService mailTrapService;

    private final URI passwordResetLinkBaseUrl = URI.create("https://example.com/reset-password");
    private final Map<EmailType, String> templates = Map.of(
            EmailType.PASSWORD_RESET, "2f232013-a951-439a-9262-82175d69b512"
    );

    private SendPasswordResetEmailService sendPasswordResetEmailService;

    @BeforeEach
    void setUp() {
        sendPasswordResetEmailService = new SendPasswordResetEmailService(
                mailTrapService,
                passwordResetLinkBaseUrl,
                templates
        );
    }

    @Test
    void execute_WhenMailtrapSucceeds_ReturnsTrueAndSendsPasswordResetEmail() {
        SendResetPasswordEmailCommand cmd = new SendResetPasswordEmailCommand(
                "user@userid.com",
                "reset-token-123"
        );

        SendResponse response = new SendResponse();
        response.setSuccess(true);
        when(mailTrapService.sendEmail(anyString(), anyString(), anyMap()))
                .thenReturn(response);

        boolean result = sendPasswordResetEmailService.execute(cmd);

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
                () -> assertEquals("user@userid.com", recipientCaptor.getValue()),
                () -> assertEquals("2f232013-a951-439a-9262-82175d69b512", templateCaptor.getValue()),
                () -> assertEquals("user@userid.com", variables.get("email")),
                () -> assertEquals(
                        "https://example.com/reset-password?password_reset_token=reset-token-123",
                        variables.get("password_reset_link")),
                () -> assertEquals(2, variables.size())
        );
    }

    @Test
    void execute_WhenMailtrapFails_ReturnsFalse() {
        SendResetPasswordEmailCommand cmd = new SendResetPasswordEmailCommand(
                "user@userid.com",
                "reset-token-123"
        );

        SendResponse response = new SendResponse();
        response.setSuccess(false);
        when(mailTrapService.sendEmail(anyString(), anyString(), anyMap()))
                .thenReturn(response);

        boolean result = sendPasswordResetEmailService.execute(cmd);

        assertFalse(result);
    }

    @Test
    void execute_WhenMailtrapThrows_PropagatesException() {
        SendResetPasswordEmailCommand cmd = new SendResetPasswordEmailCommand(
                "user@userid.com",
                "reset-token-123"
        );

        RuntimeException boom = new RuntimeException("mailtrap down");
        when(mailTrapService.sendEmail(anyString(), anyString(), anyMap()))
                .thenThrow(boom);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> sendPasswordResetEmailService.execute(cmd)
        );
        assertEquals(boom, thrown);
    }

    @Test
    void execute_WhenPasswordResetTemplateIsMissing_ThrowsIllegalStateException() {
        SendPasswordResetEmailService service = new SendPasswordResetEmailService(
                mailTrapService,
                passwordResetLinkBaseUrl,
                Map.of()
        );
        SendResetPasswordEmailCommand cmd = new SendResetPasswordEmailCommand(
                "user@userid.com",
                "reset-token-123"
        );

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> service.execute(cmd)
        );

        assertEquals("No Mailtrap template configured for PASSWORD_RESET", thrown.getMessage());
    }
}
