package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.dto.command.SendResetPasswordEmailCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.port.in.usecase.SendPasswordResetEmailUseCase;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

public class SendPasswordResetEmailService implements SendPasswordResetEmailUseCase {

    private final MailTrapService mailTrapService;
    private final URI passwordResetLinkBaseUrl;
    private final Map<EmailType, String> templates;

    public SendPasswordResetEmailService(MailTrapService mailTrapService, URI passwordResetLinkBaseUrl, Map<EmailType, String> templates) {
        this.mailTrapService = mailTrapService;
        this.passwordResetLinkBaseUrl = passwordResetLinkBaseUrl;
        this.templates = templates;
    }

    @Override
    public boolean execute(SendResetPasswordEmailCommand cmd) {
        var templateId = templates.get(EmailType.PASSWORD_RESET);
        if (templateId == null) {
            throw new IllegalStateException("No Mailtrap template configured for " + EmailType.PASSWORD_RESET);
        }
        var response = mailTrapService.sendEmail(
                cmd.email(),
                templateId,
                getTemplateVariables(cmd)
        );
        return response.isSuccess();
    }

    private Map<String, Object> getTemplateVariables(SendResetPasswordEmailCommand cmd) {
        return Map.of(
                "email", cmd.email(),
                "password_reset_link", UriComponentsBuilder.fromUri(passwordResetLinkBaseUrl)
                        .queryParam("password_reset_token", cmd.passwordResetToken())
                        .build()
                        .toUriString()
        );
    }
}
