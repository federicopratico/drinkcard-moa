package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.out.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SumUpCreateCheckoutResponse(
        String id,
        String status,
        @JsonProperty("hosted_checkout_url") String hostedCheckoutUrl
) {
}
