package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.payment;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.HostedCheckout;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.HostedCheckoutRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGateway;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.payment.PaymentGatewayStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.payment.dto.SumUpCheckoutStatusResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.payment.dto.SumUpCreateCheckoutRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.payment.dto.SumUpCreateCheckoutResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.config.SumUpProperties;
import org.springframework.web.client.RestClient;

public class SumUpPaymentAdapter implements PaymentGateway {

    private final RestClient restClient;
    private final SumUpProperties properties;

    public SumUpPaymentAdapter(RestClient restClient, SumUpProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public HostedCheckout createHostedCheckout(HostedCheckoutRequest request) {
        SumUpCreateCheckoutRequest body = new SumUpCreateCheckoutRequest(
                request.clientReferenceId(),
                request.amount(),
                request.currency(),
                properties.getMerchantCode(),
                request.description(),
                request.redirectUrl(),
                request.returnUrl(),
                request.validUntil().toString(),
                new SumUpCreateCheckoutRequest.HostedCheckout(true)
        );

        SumUpCreateCheckoutResponse response = restClient.post()
                .uri("/v0.1/checkouts")
                .body(body)
                .retrieve()
                .body(SumUpCreateCheckoutResponse.class);

        if (response == null || response.id() == null || response.hostedCheckoutUrl() == null || response.providerCreatedAt() == null) {
            throw new IllegalStateException("Invalid response from SumUp checkout creation");
        }

        return new HostedCheckout(
                response.id(),
                response.hostedCheckoutUrl(),
                response.providerCreatedAt()
        );
    }

    @Override
    public PaymentGatewayStatus fetchCheckoutStatus(String providerCheckoutId) {
        SumUpCheckoutStatusResponse response = restClient.get()
                .uri("/v0.1/checkouts/{id}", providerCheckoutId)
                .retrieve()
                .body(SumUpCheckoutStatusResponse.class);

        if (response == null || response.status() == null) {
            return PaymentGatewayStatus.UNKNOWN;
        }

        return mapStatus(response.status());
    }

    private PaymentGatewayStatus mapStatus(String sumUpStatus) {
        return switch (sumUpStatus.toUpperCase()) {
            case "PAID", "SUCCESSFUL" -> PaymentGatewayStatus.PAID;
            case "FAILED" -> PaymentGatewayStatus.FAILED;
            case "EXPIRED" -> PaymentGatewayStatus.EXPIRED;
            case "PENDING" -> PaymentGatewayStatus.PENDING;
            default -> PaymentGatewayStatus.UNKNOWN;
        };
    }
}
