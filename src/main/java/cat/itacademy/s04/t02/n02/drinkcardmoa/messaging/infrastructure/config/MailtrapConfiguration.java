package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.infrastructure.config;

import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.service.MailTrapService;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({MailtrapProperties.class})
public class MailtrapConfiguration {

    @Bean
    public MailTrapService mailTrapService(
            MailtrapProperties mailtrapProperties
    ) {
        var config = new MailtrapConfig.Builder()
                .token(mailtrapProperties.getToken())
                .build();

        return new MailTrapService(
                MailtrapClientFactory.createMailtrapClient(config),
                mailtrapProperties.getSenderEmail());
    }

}
