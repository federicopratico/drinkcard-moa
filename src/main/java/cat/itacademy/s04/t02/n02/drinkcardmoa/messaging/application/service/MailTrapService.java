package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.service;


import io.mailtrap.client.MailtrapClient;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import io.mailtrap.model.response.emails.SendResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MailTrapService {
    private final MailtrapClient mailtrapClient;
    private final String senderEmail;

    SendResponse sendEmail(String recipientEmail, String templateId, Map<String, Object> variables) {
        var mail = MailtrapMail.builder()
                .from(new Address(senderEmail))
                .to(List.of(new Address(recipientEmail)))
                .templateUuid(templateId)
                .templateVariables(variables)
                .build();

        return mailtrapClient.send(mail);
    }
}
