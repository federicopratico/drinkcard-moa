package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "password-reset")
public record PasswordResetProperties(long expirationMinutes) {
}
