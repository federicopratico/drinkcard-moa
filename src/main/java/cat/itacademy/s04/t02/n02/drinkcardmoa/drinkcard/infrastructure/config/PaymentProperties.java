package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {

    private String frontendSuccessUrl;
    private Duration checkoutExpiration;
    private String webhookReturnUrl;
}
