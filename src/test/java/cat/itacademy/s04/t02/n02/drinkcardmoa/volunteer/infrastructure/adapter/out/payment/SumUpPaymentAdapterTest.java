package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.out.payment;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.payment.HostedCheckout;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.payment.HostedCheckoutRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.payment.PaymentGatewayStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.config.SumUpProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SumUpPaymentAdapterTest {

    private MockRestServiceServer server;
    private SumUpPaymentAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.sumup.test");

        server = MockRestServiceServer.bindTo(builder).build();

        SumUpProperties properties = new SumUpProperties();
        properties.setMerchantCode("merchant-123");

        adapter = new SumUpPaymentAdapter(builder.build(), properties);
    }

    @Test
    void createHostedCheckout_WhenSumUpReturnsValidResponse_ShouldReturnHostedCheckout() {
        server.expect(requestTo("https://api.sumup.test/v0.1/checkouts"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "checkout_reference": "payment-123",
                          "amount": 10,
                          "currency": "EUR",
                          "merchant_code": "merchant-123",
                          "description": "Drink card - 5 credits",
                          "redirect_url": "http://localhost:3000/payment/success",
                          "hosted_checkout": {
                            "enabled": true
                          }
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "id": "checkout-123",
                          "status": "PENDING",
                          "hosted_checkout_url": "https://checkout.sumup.com/checkout-123"
                        }
                        """, MediaType.APPLICATION_JSON));

        HostedCheckout result = adapter.createHostedCheckout(checkoutRequest());

        assertAll(
                () -> assertEquals("checkout-123", result.providerCheckoutId()),
                () -> assertEquals("https://checkout.sumup.com/checkout-123", result.checkoutUrl())
        );

        server.verify();
    }

    @Test
    void createHostedCheckout_WhenResponseIsNull_ShouldThrowIllegalStateException() {
        server.expect(requestTo("https://api.sumup.test/v0.1/checkouts"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class, () -> adapter.createHostedCheckout(checkoutRequest()));

        server.verify();
    }

    @Test
    void createHostedCheckout_WhenResponseHasNoId_ShouldThrowIllegalStateException() {
        server.expect(requestTo("https://api.sumup.test/v0.1/checkouts"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "status": "PENDING",
                          "hosted_checkout_url": "https://checkout.sumup.com/checkout-123"
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class, () -> adapter.createHostedCheckout(checkoutRequest()));

        server.verify();
    }

    @Test
    void createHostedCheckout_WhenResponseHasNoHostedCheckoutUrl_ShouldThrowIllegalStateException() {
        server.expect(requestTo("https://api.sumup.test/v0.1/checkouts"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "id": "checkout-123",
                          "status": "PENDING"
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class, () -> adapter.createHostedCheckout(checkoutRequest()));

        server.verify();
    }

    @ParameterizedTest
    @CsvSource({
            "PAID, PAID",
            "SUCCESSFUL, PAID",
            "FAILED, FAILED",
            "EXPIRED, EXPIRED",
            "PENDING, PENDING",
            "CANCELLED, UNKNOWN"
    })
    void fetchCheckoutStatus_ShouldMapSumUpStatusToPaymentGatewayStatus(
            String sumUpStatus,
            PaymentGatewayStatus expectedStatus
    ) {
        server.expect(requestTo("https://api.sumup.test/v0.1/checkouts/checkout-123"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "id": "checkout-123",
                          "status": "%s",
                          "checkout_reference": "payment-123",
                          "transaction_id": "transaction-123",
                          "transaction_code": "transaction-code-123"
                        }
                        """.formatted(sumUpStatus), MediaType.APPLICATION_JSON));

        PaymentGatewayStatus result = adapter.fetchCheckoutStatus("checkout-123");

        assertEquals(expectedStatus, result);

        server.verify();
    }

    @Test
    void fetchCheckoutStatus_WhenResponseIsNull_ShouldReturnUnknown() {
        server.expect(requestTo("https://api.sumup.test/v0.1/checkouts/checkout-123"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        PaymentGatewayStatus result = adapter.fetchCheckoutStatus("checkout-123");

        assertEquals(PaymentGatewayStatus.UNKNOWN, result);

        server.verify();
    }

    @Test
    void fetchCheckoutStatus_WhenResponseHasNoStatus_ShouldReturnUnknown() {
        server.expect(requestTo("https://api.sumup.test/v0.1/checkouts/checkout-123"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "id": "checkout-123"
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentGatewayStatus result = adapter.fetchCheckoutStatus("checkout-123");

        assertEquals(PaymentGatewayStatus.UNKNOWN, result);

        server.verify();
    }

    private HostedCheckoutRequest checkoutRequest() {
        return new HostedCheckoutRequest(
                "payment-123",
                BigDecimal.valueOf(10),
                "EUR",
                "Drink card - 5 credits",
                "http://localhost:3000/payment/success"
        );
    }
}
