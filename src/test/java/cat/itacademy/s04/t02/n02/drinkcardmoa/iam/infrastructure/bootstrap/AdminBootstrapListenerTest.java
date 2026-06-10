package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.bootstrap;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.BootstrapAdminCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.BootstrapAdminUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AdminBootstrapListenerTest {

    private BootstrapAdminUseCase bootstrapAdminUseCase;
    private AdminBootstrapProperties properties;
    private AdminBootstrapListener listener;

    @BeforeEach
    void setUp() {
        bootstrapAdminUseCase = mock(BootstrapAdminUseCase.class);
        properties = new AdminBootstrapProperties();

        listener = new AdminBootstrapListener(
                bootstrapAdminUseCase,
                properties
        );
    }

    @Test
    void onApplicationReady_WhenBootstrapDisabled_DoesNotExecuteUseCase() {
        properties.setEnabled(false);

        listener.onApplicationReady();

        verifyNoInteractions(bootstrapAdminUseCase);
    }

    @Test
    void onApplicationReady_WhenBootstrapEnabled_ExecutesUseCaseWithConfiguredValues() {
        properties.setEnabled(true);
        properties.setFirstName("System");
        properties.setLastName("Admin");
        properties.setEmail("admin@test.com");
        properties.setPassword("SecurePassword123!");

        listener.onApplicationReady();

        ArgumentCaptor<BootstrapAdminCommand> captor =
                ArgumentCaptor.forClass(BootstrapAdminCommand.class);

        verify(bootstrapAdminUseCase).execute(captor.capture());

        BootstrapAdminCommand command = captor.getValue();

        assertAll(
                () -> assertEquals("System", command.firstName()),
                () -> assertEquals("Admin", command.lastName()),
                () -> assertEquals("admin@test.com", command.email()),
                () -> assertEquals("SecurePassword123!", command.password())
        );
    }
}