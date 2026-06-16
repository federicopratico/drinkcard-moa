package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result;

public record RefreshTokenResult(
        String accessToken,
        String refreshToken,
        String volunteerId,
        String email,
        String role
) {
}
