package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.CurrentUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.CurrentUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.GetCurrentUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.CurrentUserResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final UserMapper mapper;

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(Authentication authentication) {

        CurrentUserCommand command = mapper.toCommand(authentication.getName());
        CurrentUserResult result = getCurrentUserUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.OK).body(mapper.toResponse(result));
    }
}
