package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query;

public record ListUsersQuery(
        String role,
        String status,
        String email,
        int page,
        int size,
        String sort
) {
}
