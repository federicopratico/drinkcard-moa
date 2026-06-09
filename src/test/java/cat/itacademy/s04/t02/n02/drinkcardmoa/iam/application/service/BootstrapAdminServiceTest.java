package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.BootstrapAdminCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordEncoder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BootstrapAdminServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private BootstrapAdminService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new BootstrapAdminService(userRepository, passwordEncoder);
    }

    @Test
    void execute_WhenAdminEmailAlreadyExists_DoesNotCreateAdmin() {
        var command = command();

        when(userRepository.existsByEmail(Email.from(command.email())))
                .thenReturn(true);

        service.execute(command);

        verify(userRepository).existsByEmail(Email.from(command.email()));
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }

    @Test
    void execute_WhenAdminEmailDoesNotExist_CreatesAdminUser() {
        var command = command();

        when(userRepository.existsByEmail(Email.from(command.email())))
                .thenReturn(false);
        when(passwordEncoder.encode(command.password()))
                .thenReturn("hashed-password");

        service.execute(command);

        verify(passwordEncoder).encode(command.password());
        verify(userRepository).save(argThat(user ->
                user.getEmail().asString().equals(command.email())
                        && user.getFullName().asString().equals("System Admin")
                        && user.getHashedPassword().value().equals("hashed-password")
                        && user.getRole() == Role.ADMIN
                        && user.isActive()
        ));
    }

    private BootstrapAdminCommand command() {
        return new BootstrapAdminCommand(
                "System",
                "Admin",
                "SecurePassword123!",
                "admin@test.com"
        );
    }
}