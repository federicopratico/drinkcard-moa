package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment;

import java.math.BigDecimal;

public interface PaymentGateway {
    HostedCheckout createHostedCheckout(HostedCheckoutRequest request);
    PaymentGatewayStatus fetchCheckoutStatus(String providerCheckoutId);
}
