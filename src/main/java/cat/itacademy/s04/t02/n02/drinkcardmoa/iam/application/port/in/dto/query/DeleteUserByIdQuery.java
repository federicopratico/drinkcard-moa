package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query;

public record DeleteUserByIdQuery(
        String deletedBy,
        String userId
) {
}
