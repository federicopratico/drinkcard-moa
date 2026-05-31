package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query;

public record GetPaymentStatusQuery(
        String paymentId,
        String volunteerId
) {
}
