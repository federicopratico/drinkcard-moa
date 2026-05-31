package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

public record SumUpCreateCheckoutRequest(
        @JsonProperty("checkout_reference") String checkoutReference,
        BigDecimal amount,
        String currency,
        @JsonProperty("merchant_code") String merchantCode,
        String description,
        @JsonProperty("redirect_url") String redirectUrl,
        @JsonProperty("return_url") String returnUrl,
        @JsonProperty("valid_until") String validUntil,
        @JsonProperty("hosted_checkout") HostedCheckout hostedCheckout
) {
    public record HostedCheckout(
            boolean enabled
    ) {}
}
