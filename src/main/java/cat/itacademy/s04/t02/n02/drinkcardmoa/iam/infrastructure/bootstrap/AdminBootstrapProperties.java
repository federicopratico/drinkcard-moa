package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.bootstrap;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.bootstrap-admin")
public class AdminBootstrapProperties {
    private boolean enabled;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
