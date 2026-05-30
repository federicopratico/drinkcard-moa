package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response;

public record InvitationResponse(
        String id,
        String email,
        String role,
        String status
) {
}
