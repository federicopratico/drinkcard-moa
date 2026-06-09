package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.bootstrap;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.BootstrapAdminCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.BootstrapAdminUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminBootstrapProperties.class)
public class AdminBootstrapListener {

    private final BootstrapAdminUseCase bootstrapAdminUseCase;
    private final AdminBootstrapProperties adminBootstrapProperties;
    private final LockRegistry lockRegistry;

    @EventListener(ApplicationReadyEvent.class)
    void onApplicationReady() {
        if (!adminBootstrapProperties.isEnabled()) {
            return;
        }

        Lock lock = lockRegistry.obtain("admin.creation.lock");

        if(!lock.tryLock())
            return;

        try {
           bootstrapAdminUseCase.execute(toCommand(adminBootstrapProperties));
        } finally {
            lock.unlock();
        }
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
