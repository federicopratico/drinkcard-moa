package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.bootstrap;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.BootstrapAdminCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.BootstrapAdminUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminBootstrapProperties.class)
public class AdminBootstrapListener {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapListener.class);

    private final BootstrapAdminUseCase bootstrapAdminUseCase;
    private final AdminBootstrapProperties adminBootstrapProperties;

    @EventListener(ApplicationReadyEvent.class)
    void onApplicationReady() {

        if (!adminBootstrapProperties.isEnabled()) {
            return;
        }

        log.info("AdminBootstrapListener creating new admin user after ApplicationReadyEvent.");

        bootstrapAdminUseCase.execute(toCommand(adminBootstrapProperties));
    }

    private BootstrapAdminCommand toCommand(AdminBootstrapProperties properties) {
        return new BootstrapAdminCommand(
                properties.getFirstName(),
                properties.getLastName(),
                properties.getPassword(),
                properties.getEmail()
        );
    }
}
