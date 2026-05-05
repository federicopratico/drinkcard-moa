package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result;

public record RegisterUserResult(
        String id,
        String firstName,
        String lastName,
        String email
) {
}
