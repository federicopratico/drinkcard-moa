package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.LoginUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.LogoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.RefreshTokenCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.RegisterUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.InitiatePasswordResetCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.ResetPasswordCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.LoginUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RefreshTokenResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RegisterUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.*;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.request.*;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.LoginResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.RefreshTokenResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.RegisterResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper.AuthMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private AuthenticateUserUseCase authenticateUserUseCase;

    @Mock
    private RefreshAccessTokenUseCase refreshAccessTokenUseCase;

    @Mock
    private LogoutUseCase logoutUseCase;

    @Mock
    private InitiatePasswordResetUseCase initiatePasswordResetUseCase;

    @Mock
    private ResetPasswordUseCase resetPasswordUseCase;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(
                registerUserUseCase,
                authenticateUserUseCase,
                refreshAccessTokenUseCase,
                logoutUseCase,
                initiatePasswordResetUseCase,
                resetPasswordUseCase,
                new AuthMapper()
        );
    }

    @Test
    void register_WhenRequestIsValid_ReturnsCreatedRegisterResponse() {
        RegisterRequest request = new RegisterRequest(
                "First",
                "Last",
                "password123",
                "invitation-token"
        );
        RegisterUserResult result = new RegisterUserResult(
                "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1",
                "First",
                "Last",
                "user@email.com",
                "VOLUNTEER"
        );

        when(registerUserUseCase.execute(new RegisterUserCommand(
                "First",
                "Last",
                "password123",
                "VOLUNTEER",
                "invitation-token"
        ))).thenReturn(result);

        ResponseEntity<RegisterResponse> response = controller.register(request);

        ArgumentCaptor<RegisterUserCommand> commandCaptor =
                ArgumentCaptor.forClass(RegisterUserCommand.class);

        verify(registerUserUseCase).execute(commandCaptor.capture());

        RegisterResponse body = response.getBody();
        RegisterUserCommand command = commandCaptor.getValue();

        assertAll(
                () -> assertEquals(201, response.getStatusCode().value()),
                () -> assertNotNull(body),
                () -> assertEquals("user@email.com", body.email()),
                () -> assertEquals("First", body.firstName()),
                () -> assertEquals("Last", body.lastName()),
                () -> assertEquals("First", command.firstName()),
                () -> assertEquals("Last", command.lastName()),
                () -> assertEquals("password123", command.password()),
                () -> assertEquals("VOLUNTEER", command.role()),
                () -> assertEquals("invitation-token", command.invitationToken())
        );
    }

    @Test
    void login_WhenCredentialsAreValid_ReturnsLoginResponseAndRefreshToken() {
        LoginRequest request = new LoginRequest(
                "user@email.com",
                "password123"
        );
        LoginUserResult result = new LoginUserResult(
                "access-token",
                "raw-refresh-token",
                "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1",
                "user@email.com",
                "VOLUNTEER"
        );

        when(authenticateUserUseCase.execute(new LoginUserCommand(
                "user@email.com",
                "password123"
        ))).thenReturn(result);

        ResponseEntity<LoginResponse> response = controller.login(request);

        ArgumentCaptor<LoginUserCommand> commandCaptor =
                ArgumentCaptor.forClass(LoginUserCommand.class);

        verify(authenticateUserUseCase).execute(commandCaptor.capture());

        LoginResponse body = response.getBody();
        LoginUserCommand command = commandCaptor.getValue();

        assertAll(
                () -> assertEquals(200, response.getStatusCode().value()),
                () -> assertNotNull(body),
                () -> assertEquals("access-token", body.token()),
                () -> assertEquals("4f0a8db1-63a7-4997-944c-9f2f6b82e6d1", body.volunteerId()),
                () -> assertEquals("user@email.com", body.email()),
                () -> assertEquals("VOLUNTEER", body.role()),
                () -> assertEquals("raw-refresh-token", body.refreshToken()),
                () -> assertEquals("user@email.com", command.email()),
                () -> assertEquals("password123", command.password())
        );
    }

    @Test
    void refresh_WhenRefreshTokenExists_ReturnsRefreshTokenResponseAndRefreshToken() {
        RefreshTokenResult result = new RefreshTokenResult(
                "new-access-token",
                "new-raw-refresh-token",
                "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1",
                "user@email.com",
                "VOLUNTEER"
        );

        when(refreshAccessTokenUseCase.execute(new RefreshTokenCommand("raw-refresh-token")))
                .thenReturn(result);

        ResponseEntity<RefreshTokenResponse> response = controller.refresh(new RefreshTokenRequest("raw-refresh-token"));

        ArgumentCaptor<RefreshTokenCommand> commandCaptor =
                ArgumentCaptor.forClass(RefreshTokenCommand.class);

        verify(refreshAccessTokenUseCase).execute(commandCaptor.capture());

        RefreshTokenResponse body = response.getBody();

        assertAll(
                () -> assertEquals(200, response.getStatusCode().value()),
                () -> assertNotNull(body),
                () -> assertEquals("new-access-token", body.token()),
                () -> assertEquals("new-raw-refresh-token", body.refreshToken()),
                () -> assertEquals("4f0a8db1-63a7-4997-944c-9f2f6b82e6d1", body.volunteerId()),
                () -> assertEquals("user@email.com", body.email()),
                () -> assertEquals("VOLUNTEER", body.role()),
                () -> assertEquals("raw-refresh-token", commandCaptor.getValue().rawRefreshToken())
        );
    }

    @Test
    void logout_WhenRefreshTokenExists_ReturnsNoContent() {
        LogoutRequest request = new LogoutRequest("raw-refresh-token");

        ResponseEntity<Void> response = controller.logout(request);

        ArgumentCaptor<LogoutCommand> commandCaptor =
                ArgumentCaptor.forClass(LogoutCommand.class);

        verify(logoutUseCase).execute(commandCaptor.capture());

        assertAll(
                () -> assertEquals(204, response.getStatusCode().value()),
                () -> assertEquals("raw-refresh-token", commandCaptor.getValue().refreshToken())
        );
    }

    @Test
    void initiatePasswordReset_ReturnsOk() {
        InitiatePasswordResetRequest request = new InitiatePasswordResetRequest("user@email.com");

        ResponseEntity<Void> response = controller.initiatePasswordReset(request);

        ArgumentCaptor<InitiatePasswordResetCommand> commandCaptor =
                ArgumentCaptor.forClass(InitiatePasswordResetCommand.class);

        verify(initiatePasswordResetUseCase).execute(commandCaptor.capture());

        assertAll(
                () -> assertEquals(200, response.getStatusCode().value()),
                () -> assertEquals("user@email.com", commandCaptor.getValue().email())
        );
    }

    @Test
    void resetPassword_ReturnsNoContent() {
        ResetPasswordRequest request = new ResetPasswordRequest("raw-password-reset-token", "newPassword123");

        ResponseEntity<Void> response = controller.resetPassword(request);

        ArgumentCaptor<ResetPasswordCommand> commandCaptor =
                ArgumentCaptor.forClass(ResetPasswordCommand.class);

        verify(resetPasswordUseCase).execute(commandCaptor.capture());

        assertAll(
                () -> assertEquals(204, response.getStatusCode().value()),
                () -> assertEquals("raw-password-reset-token", commandCaptor.getValue().rawToken()),
                () -> assertEquals("newPassword123", commandCaptor.getValue().newPassword())
        );
    }
}
