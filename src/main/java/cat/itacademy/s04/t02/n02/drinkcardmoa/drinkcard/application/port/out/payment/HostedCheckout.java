package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment;

import java.time.Instant;

public record HostedCheckout(
        String providerCheckoutId,
        String checkoutUrl,
        Instant providerCreatedAt
) {
}
