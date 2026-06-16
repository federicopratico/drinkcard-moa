package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RefreshTokenProperties.class})
public class RefreshTokenConfiguration {
}
