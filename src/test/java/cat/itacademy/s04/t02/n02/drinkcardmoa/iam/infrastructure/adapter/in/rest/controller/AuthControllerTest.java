package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.LoginUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.RefreshTokenCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.RegisterUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.LoginUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RefreshTokenResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RegisterUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.AuthenticateUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.RefreshAccessTokenUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.RegisterUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidTokenException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.request.LoginRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.request.RefreshTokenRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.request.RegisterRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.LoginResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.RefreshTokenResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.RegisterResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper.AuthMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.config.RefreshTokenProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    private AuthController controller;

    @BeforeEach
    void setUp() {
        RefreshTokenProperties refreshTokenProperties = new RefreshTokenProperties(
                30,
                new RefreshTokenProperties.Cookie(
                        "refresh_token",
                        true,
                        "Lax",
                        "/api/v1/auth"
                )
        );

        controller = new AuthController(
                registerUserUseCase,
                authenticateUserUseCase,
                refreshAccessTokenUseCase,
                refreshTokenProperties,
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
    void login_WhenCredentialsAreValid_ReturnsLoginResponseAndRefreshTokenCookie() {
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
    void refresh_WhenRefreshTokenCookieExists_ReturnsRefreshTokenResponseAndNewCookie() {
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
        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

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
}
