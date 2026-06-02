package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.InviteUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.GetUserByIdQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.GetUserByIdUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.InviteUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.ListUsersUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.request.InvitationRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.InvitationResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.UserSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper.AdminUserMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper.InvitationMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/invitations")
@AllArgsConstructor
public class AdminInvitationController {

    private final InviteUserUseCase inviteUserUseCase;
    private final InvitationMapper mapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvitationResponse> inviteUser(
            @Valid @RequestBody InvitationRequest invitationRequest) {
        var result = inviteUserUseCase.execute(new InviteUserCommand(invitationRequest.email(), invitationRequest.role()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
    }
}
