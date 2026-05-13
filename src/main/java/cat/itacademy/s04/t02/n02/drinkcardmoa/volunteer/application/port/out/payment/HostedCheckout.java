package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.payment;

public record HostedCheckout(
        String providerCheckoutId,
        String checkoutUrl
) {
}
