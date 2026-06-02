package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.InviteUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.InvitationResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.InviteUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.request.InvitationRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.InvitationResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper.InvitationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminInvitationControllerTest {

    @Mock
    private InviteUserUseCase inviteUserUseCase;

    private AdminInvitationController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminInvitationController(inviteUserUseCase, new InvitationMapper());
    }

    @Test
    void inviteUser_WhenValidRequest_Returns201CreatedWithInvitationResponse() {
        InvitationRequest request = new InvitationRequest("invitee@userid.com", "VOLUNTEER");
        InvitationResult result = new InvitationResult(
                "11111111-2222-3333-4444-555555555555",
                "invitee@userid.com",
                "VOLUNTEER",
                "PENDING"
        );

        when(inviteUserUseCase.execute(new InviteUserCommand("invitee@userid.com", "VOLUNTEER")))
                .thenReturn(result);

        ResponseEntity<InvitationResponse> response = controller.inviteUser(request);

        ArgumentCaptor<InviteUserCommand> commandCaptor = ArgumentCaptor.forClass(InviteUserCommand.class);
        verify(inviteUserUseCase).execute(commandCaptor.capture());

        InvitationResponse body = response.getBody();
        InviteUserCommand command = commandCaptor.getValue();

        assertAll(
                () -> assertEquals(201, response.getStatusCode().value()),
                () -> assertNotNull(body),
                () -> assertEquals(result.id(), body.id()),
                () -> assertEquals(result.email(), body.email()),
                () -> assertEquals(result.role(), body.role()),
                () -> assertEquals(result.status(), body.status()),
                () -> assertEquals("invitee@userid.com", command.email()),
                () -> assertEquals("VOLUNTEER", command.role())
        );
    }

    @Test
    void inviteUser_ShouldRequireAdminRole() throws NoSuchMethodException {
        Method method = AdminInvitationController.class.getMethod("inviteUser", InvitationRequest.class);

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertAll(
                () -> assertNotNull(preAuthorize),
                () -> assertEquals("hasRole('ADMIN')", preAuthorize.value())
        );
    }
}
