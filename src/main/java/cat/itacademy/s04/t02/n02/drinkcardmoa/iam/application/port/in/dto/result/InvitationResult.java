package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.Invitation;

public record InvitationResult(
        String id,
        String email,
        String role,
        String status
)
{
    public static InvitationResult from(Invitation invitation) {
        return new InvitationResult(
                invitation.getId().asString(),
                invitation.getEmail().asString(),
                invitation.getRole().name(),
                invitation.getStatus().name()
        );
    }
}
