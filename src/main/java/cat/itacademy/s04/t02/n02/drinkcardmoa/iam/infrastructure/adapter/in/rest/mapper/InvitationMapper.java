package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.CurrentUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.CurrentUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.InvitationResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.CurrentUserResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.InvitationResponse;
import org.springframework.stereotype.Component;

@Component
public class InvitationMapper {

    public InvitationResponse toResponse(InvitationResult result) {
        return new InvitationResponse(
                result.id(),
                result.email(),
                result.role(),
                result.status()
        );
    }
}
