package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response;

public record RefreshTokenResponse(
        String token,
        String volunteerId,
        String email,
        String role
) {
}
