package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.service;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.model.request.emails.MailtrapMail;
import io.mailtrap.model.response.emails.SendResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailTrapServiceTest {

    @Mock
    private MailtrapClient mailtrapClient;

    @Test
    void sendEmail_BuildsMailtrapMailWithSenderRecipientTemplateAndVariables() {
        MailTrapService service = new MailTrapService(mailtrapClient, "noreply@drinkcardmoa.com");

        SendResponse expected = new SendResponse();
        expected.setSuccess(true);
        when(mailtrapClient.send(any(MailtrapMail.class))).thenReturn(expected);

        Map<String, Object> variables = Map.of("role", "VOLUNTEER", "invitation_token", "tok");

        SendResponse actual = service.sendEmail(
                "to@userid.com",
                "template-uuid",
                variables
        );

        ArgumentCaptor<MailtrapMail> mailCaptor = ArgumentCaptor.forClass(MailtrapMail.class);
        verify(mailtrapClient).send(mailCaptor.capture());
        MailtrapMail sent = mailCaptor.getValue();

        assertAll(
                () -> assertSame(expected, actual),
                () -> assertEquals("noreply@drinkcardmoa.com", sent.getFrom().getEmail()),
                () -> assertEquals(1, sent.getTo().size()),
                () -> assertEquals("to@userid.com", sent.getTo().getFirst().getEmail()),
                // Mailtrap rejects the request when both subject and templateUuid are set;
                // when using a template the subject is owned by the template itself.
                () -> org.junit.jupiter.api.Assertions.assertNull(sent.getSubject()),
                () -> assertEquals("template-uuid", sent.getTemplateUuid()),
                () -> assertEquals(variables, sent.getTemplateVariables())
        );
    }
}
