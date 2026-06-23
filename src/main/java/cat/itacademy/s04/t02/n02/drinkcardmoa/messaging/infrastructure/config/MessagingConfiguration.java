package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.infrastructure.config;

import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.service.MailTrapService;
import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.service.SendInvitationEmailService;
import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.service.SendPasswordResetEmailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfiguration {

    @Bean
    public SendInvitationEmailService sendInvitationEmailService(
            MailtrapProperties mailtrapProperties,
            MailTrapService mailTrapService
    ) {
        return new SendInvitationEmailService(
                mailTrapService,
                mailtrapProperties.getRegistrationLinkBaseUrl(),
                mailtrapProperties.getTemplates()
        );
    }

    @Bean
    public SendPasswordResetEmailService sendPasswordResetEmailService(
            MailtrapProperties mailtrapProperties,
            MailTrapService mailTrapService
    ) {
        return new SendPasswordResetEmailService(
                mailTrapService,
                mailtrapProperties.getPasswordResetLinkBaseUrl(),
                mailtrapProperties.getTemplates()
        );
    }
}
