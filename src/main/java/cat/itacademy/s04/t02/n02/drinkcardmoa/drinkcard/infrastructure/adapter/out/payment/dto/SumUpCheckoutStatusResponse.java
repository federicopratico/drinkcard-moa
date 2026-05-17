package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SumUpCheckoutStatusResponse(
        String id,
        String status,
        @JsonProperty("checkout_reference") String checkoutReference,
        @JsonProperty("transaction_id") String transactionId,
        @JsonProperty("transaction_code") String transactionCode
) {
}
