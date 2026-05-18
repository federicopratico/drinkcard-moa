package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.ListUsersUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.UserSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper.AdminUserMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@AllArgsConstructor
public class AdminUserController {

    private final ListUsersUseCase listUsersUseCase;
    private final AdminUserMapper mapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryResponse>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String email) {

        List<UserSummaryResult> result = listUsersUseCase.execute(mapper.toQuery(role, status, email));

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        result.stream()
                                .map(mapper::toResponse)
                                .toList());
    }
}
