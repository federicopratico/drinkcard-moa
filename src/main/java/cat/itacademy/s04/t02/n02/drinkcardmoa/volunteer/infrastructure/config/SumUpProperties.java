package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "sumup")
public class SumUpProperties {
    private String baseUrl;
    private String apiKey;
    private String merchantCode;
}
