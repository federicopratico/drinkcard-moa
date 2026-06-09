package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.bootstrap;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.BootstrapAdminUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.integration.support.locks.LockRegistry;

import java.util.concurrent.locks.Lock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminBootstrapListenerTest {

    private BootstrapAdminUseCase bootstrapAdminUseCase;
    private AdminBootstrapProperties properties;
    private LockRegistry lockRegistry;
    private Lock lock;
    private AdminBootstrapListener listener;

    @BeforeEach
    void setUp() {
        bootstrapAdminUseCase = mock(BootstrapAdminUseCase.class);
        properties = new AdminBootstrapProperties();
        lockRegistry = mock(LockRegistry.class);
        lock = mock(Lock.class);

        listener = new AdminBootstrapListener(
                bootstrapAdminUseCase,
                properties,
                lockRegistry
        );
    }

    @Test
    void onApplicationReady_WhenBootstrapDisabled_DoesNotAcquireLockAndDoesNotExecuteUseCase() {
        properties.setEnabled(false);

        listener.onApplicationReady();

        verifyNoInteractions(lockRegistry);
        verifyNoInteractions(bootstrapAdminUseCase);
    }

    @Test
    void onApplicationReady_WhenLockCannotBeAcquired_DoesNotExecuteUseCase() {
        properties.setEnabled(true);
        when(lockRegistry.obtain("admin.creation.lock")).thenReturn(lock);
        when(lock.tryLock()).thenReturn(false);

        listener.onApplicationReady();

        verify(lockRegistry).obtain("admin.creation.lock");
        verify(lock).tryLock();
        verifyNoInteractions(bootstrapAdminUseCase);
        verify(lock, never()).unlock();
    }

    @Test
    void onApplicationReady_WhenEnabledAndLockAcquired_ExecutesUseCaseAndReleasesLock() {
        properties.setEnabled(true);
        properties.setEmail("admin@test.com");
        properties.setPassword("SecurePassword123!");
        properties.setFirstName("System");
        properties.setLastName("Admin");

        when(lockRegistry.obtain("admin.creation.lock")).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);

        listener.onApplicationReady();

        verify(bootstrapAdminUseCase).execute(argThat(command ->
                command.firstName().equals("System")
                        && command.lastName().equals("Admin")
                        && command.email().equals("admin@test.com")
                        && command.password().equals("SecurePassword123!")
        ));
        verify(lock).unlock();
    }

    @Test
    void onApplicationReady_WhenUseCaseThrows_StillReleasesLock() {
        properties.setEnabled(true);
        properties.setEmail("admin@test.com");
        properties.setPassword("SecurePassword123!");
        properties.setFirstName("System");
        properties.setLastName("Admin");

        when(lockRegistry.obtain("admin.creation.lock")).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);

        doThrow(new RuntimeException("boom"))
                .when(bootstrapAdminUseCase)
                .execute(any());

        try {
            listener.onApplicationReady();
        } catch (RuntimeException ignored) {
        }

        verify(lock).unlock();
    }
}