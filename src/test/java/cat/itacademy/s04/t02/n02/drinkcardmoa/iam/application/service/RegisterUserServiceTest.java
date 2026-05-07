package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.RegisterUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RegisterUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordEncoder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.EmailAlreadyExistsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserService registerUserService;


    @Test
    void execute_WhenEmailDoesNotExistAndValidInput_CreateNewUser() {
        RegisterUserCommand cmd = new RegisterUserCommand(
                "first",
                "last",
                "first_last@email.com",
                "password",
                "VOLUNTEER");

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(i-> i.getArguments()[0]);

        RegisterUserResult result = registerUserService.execute(cmd);

        assertNotNull(result);
        assertEquals(cmd.firstName(), result.firstName());
        assertEquals(cmd.lastName(), result.lastName());
        assertEquals(cmd.email(), result.email());

        verify(userRepository, times(1)).existsByEmail(any(Email.class));
        verify(passwordEncoder, times(1)).encode(cmd.password());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void execute_WhenEmailAlreadyExists_ThrowEmailAlreadyExistsException() {
        RegisterUserCommand cmd = new RegisterUserCommand(
                "first",
                "last",
                "duplicate@email.com",
                "password",
                "VOLUNTEER");

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> {
            registerUserService.execute(cmd);
        });

        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }
}