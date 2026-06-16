package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response;

public record LoginResponse(
        String token,
        String refreshToken,
        String volunteerId,
        String email,
        String role
) {
}
