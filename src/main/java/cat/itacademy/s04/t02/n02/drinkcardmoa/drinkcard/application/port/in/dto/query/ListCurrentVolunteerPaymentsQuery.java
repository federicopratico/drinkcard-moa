package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query;

public record ListCurrentVolunteerPaymentsQuery(
        String volunteerId,
        int page,
        int size,
        String sort
) {
}
