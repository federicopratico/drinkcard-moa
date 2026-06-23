package cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.infrastructure.config;

import cat.itacademy.s04.t02.n02.drinkcardmoa.messaging.application.service.EmailType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.util.EnumMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "mailtrap")
public class MailtrapProperties {
    private String token;
    private String senderEmail;
    private URI registrationLinkBaseUrl;
    private URI PasswordResetLinkBaseUrl;
    private Map<EmailType, String> templates = new EnumMap<>(EmailType.class);
}
