package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response;

public record UserSummaryResponse(
        String userId,
        String fullName,
        String email,
        String role,
        String status
) {
}
