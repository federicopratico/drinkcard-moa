package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command.SendInvitationEmailCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.usecase.SendInvitationEmailUseCase;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

public class SendInvitationEmailService implements SendInvitationEmailUseCase {
    private final MailTrapService mailTrapService;
    private final URI registrationLinkBaseUrl;
    private final Map<EmailType, String> templates;

    public SendInvitationEmailService(
            MailTrapService mailTrapService,
            URI registrationLinkBaseUrl,
            Map<EmailType, String> templates
    ) {
        this.mailTrapService = mailTrapService;
        this.registrationLinkBaseUrl = registrationLinkBaseUrl;
        this.templates = templates;
    }

    @Override
    public boolean execute(SendInvitationEmailCommand cmd) {
        var templateId = templates.get(EmailType.INVITATION);
        if (templateId == null) {
            throw new IllegalStateException("No Mailtrap template configured for " + EmailType.INVITATION);
        }
        var response = mailTrapService.sendEmail(
                cmd.email(),
                templateId,
                getTemplateVariables(cmd)
        );
        return response.isSuccess();
    }

    private Map<String, Object> getTemplateVariables(SendInvitationEmailCommand cmd) {
        return Map.of(
                "email", cmd.email(),
                "role", cmd.role(),
                "registration_link", UriComponentsBuilder.fromUri(registrationLinkBaseUrl)
                        .queryParam("invitation_token", cmd.invitationToken())
                        .build()
                        .toUriString()
        );
    }
}
