package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.LoginUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.LoginUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordEncoder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.TokenService;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidCredentialsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void execute_WhenCredentialsAreValid_LoginUser() {

        LoginUserCommand command = new LoginUserCommand(
                "email@email.com",
                "12345678"
        );

        User user = createUser();

        when(userRepository.findUserByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(command.password(), user.getHashedPassword().value()))
                .thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("new-token");

        LoginUserResult result = authenticationService.execute(command);

        assertNotNull(user);
        assertEquals("new-token", result.token());
        assertEquals("email@email.com", result.email());
        assertEquals(user.getRole().name(), result.role());

        verify(userRepository, times(1)).findUserByEmail(any(Email.class));
        verify(passwordEncoder, times(1))
                .matches(command.password(), user.getHashedPassword().value());
        verify(tokenService, times(1)).generateToken(user);
    }

    @Test
    void execute_WhenEmailDoesNotExist_ThrowInvalidCredentialsException() {
        LoginUserCommand cmd = new LoginUserCommand(
                "missing@email.com",
                "raw_password"
        );

        when(userRepository.findUserByEmail(any(Email.class)))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.execute(cmd);
        });

        verify(userRepository, times(1)).findUserByEmail(any(Email.class));
        verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
        verify(tokenService, never()).generateToken(any(User.class));
    }

    @Test
    void execute_WhenPasswordDoesNotMatch_ThrowInvalidCredentialsException() {
        LoginUserCommand cmd = new LoginUserCommand(
                "user@email.com",
                "wrong_password"
        );

        User user = createUser();

        when(userRepository.findUserByEmail(any(Email.class)))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(cmd.password(), user.getHashedPassword().value()))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.execute(cmd);
        });

        verify(userRepository, times(1)).findUserByEmail(any(Email.class));
        verify(passwordEncoder, times(1))
                .matches(cmd.password(), user.getHashedPassword().value());
        verify(tokenService, never()).generateToken(any(User.class));
    }

    @Test
    void execute_WhenUserIsSuspended_ThrowInvalidCredentialsException() {
        LoginUserCommand cmd = new LoginUserCommand(
                "user@email.com",
                "12345678"
        );

        User user = createUser(UserStatus.SUSPENDED);

        when(userRepository.findUserByEmail(any(Email.class)))
                .thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.execute(cmd);
        });

        verify(userRepository, times(1)).findUserByEmail(any(Email.class));
        verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
        verify(tokenService, never()).generateToken(any(User.class));
    }

    @Test
    void execute_WhenUserIsDeleted_ThrowInvalidCredentialsException() {
        LoginUserCommand cmd = new LoginUserCommand(
                "user@email.com",
                "12345678"
        );

        User user = createUser(UserStatus.DELETED);

        when(userRepository.findUserByEmail(any(Email.class)))
                .thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.execute(cmd);
        });

        verify(userRepository, times(1)).findUserByEmail(any(Email.class));
        verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
        verify(tokenService, never()).generateToken(any(User.class));
    }

    private User createUser() {
        return createUser(UserStatus.ACTIVE);
    }

    private User createUser(UserStatus status) {
        return User.rehydrate(
                VolunteerID.generate(),
                FullName.from("firstName", "lastName"),
                Email.from("email@email.com"),
                HashedPassword.from("12345678"),
                Role.VOLUNTEER,
                status
        );
    }
}
