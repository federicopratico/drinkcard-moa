package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record SumUpUpdateCheckoutRequest(
        @JsonProperty("valid_until") String validUntil
) {
}
