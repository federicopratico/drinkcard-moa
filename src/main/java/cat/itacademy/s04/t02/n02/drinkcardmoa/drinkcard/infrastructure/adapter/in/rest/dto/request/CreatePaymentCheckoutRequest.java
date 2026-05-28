package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request;

public record CreatePaymentCheckoutRequest(
        String idempotencyKey
) {
}
