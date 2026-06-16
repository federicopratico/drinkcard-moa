package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.LoginUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.RegisterUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.LoginUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RefreshTokenResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.RegisterUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.request.LoginRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.request.RegisterRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.LoginResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.RefreshTokenResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.RegisterResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public RegisterUserCommand toCommand(RegisterRequest request) {
        return new RegisterUserCommand(
                request.firstName(),
                request.lastName(),
                request.password(),
                "VOLUNTEER",
                request.invitationToken()
        );
    }

    public RegisterResponse toResponse(RegisterUserResult result) {
        return new RegisterResponse(
                result.email(),
                result.firstName(),
                result.lastName()
        );
    }

    public LoginUserCommand toCommand(LoginRequest request) {
        return new LoginUserCommand(
                request.email(),
                request.password()
        );
    }

    public LoginResponse toResponse(LoginUserResult result) {
        return new LoginResponse(
                result.token(),
                result.volunteerId(),
                result.email(),
                result.role()
        );
    }

    public RefreshTokenResponse toResponse(RefreshTokenResult result) {
        return new RefreshTokenResponse(
                result.accessToken(),
                result.volunteerId(),
                result.email(),
                result.role()
        );
    }
}
