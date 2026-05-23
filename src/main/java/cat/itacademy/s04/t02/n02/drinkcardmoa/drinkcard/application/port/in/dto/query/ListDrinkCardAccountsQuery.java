package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query;

public record ListDrinkCardAccountsQuery(
        String volunteerId,
        String status,
        int page,
        int size,
        String sort
) {
}
