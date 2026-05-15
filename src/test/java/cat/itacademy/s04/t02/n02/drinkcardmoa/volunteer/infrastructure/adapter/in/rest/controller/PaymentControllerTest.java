package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.security.JwtAuthenticationFilter;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.ConfirmPaymentCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.CreatePaymentCheckoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.ConfirmPaymentResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.CreatePaymentCheckoutResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase.ConfirmPaymentUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase.CreatePaymentCheckoutUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.in.rest.mapper.PaymentControllerMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.config.PaymentProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import({PaymentControllerMapper.class, PaymentProperties.class})
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "app.payment.frontend-success-url=http://localhost:3000/payment/success")
class PaymentControllerTest {

    private static final String PAYMENT_SUCCESS_URL = "http://localhost:3000/payment/success";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreatePaymentCheckoutUseCase createPaymentCheckoutUseCase;

    @MockitoBean
    private ConfirmPaymentUseCase confirmPaymentUseCase;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createCheckout_ReturnsCreatedCheckoutResponse() throws Exception {
        String volunteerId = "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1";
        String idempotencyKey = "checkout-request-123";

        CreatePaymentCheckoutResult result = new CreatePaymentCheckoutResult(
                "payment-123",
                "https://checkout.sumup.com/checkout-123",
                "PENDING",
                BigDecimal.valueOf(10)
        );

        when(createPaymentCheckoutUseCase.execute(
                new CreatePaymentCheckoutCommand(volunteerId, PAYMENT_SUCCESS_URL, idempotencyKey)
        )).thenReturn(result);

        String requestBody = objectMapper.writeValueAsString(new CreateCheckoutJson(
                volunteerId,
                idempotencyKey
        ));

        mockMvc.perform(post("/api/v1/payments/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("payment-123"))
                .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.sumup.com/checkout-123"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(10));

        ArgumentCaptor<CreatePaymentCheckoutCommand> commandCaptor =
                ArgumentCaptor.forClass(CreatePaymentCheckoutCommand.class);

        verify(createPaymentCheckoutUseCase).execute(commandCaptor.capture());

        CreatePaymentCheckoutCommand command = commandCaptor.getValue();

        assertEquals(volunteerId, command.volunteerId());
        assertEquals(PAYMENT_SUCCESS_URL, command.redirectUrl());
        assertEquals(idempotencyKey, command.idempotencyKey());
    }

    @Test
    void confirmPayment_ReturnsOkConfirmPaymentResponse() throws Exception {
        String paymentId = "payment-123";

        ConfirmPaymentResult result = new ConfirmPaymentResult(
                paymentId,
                "SUCCESS",
                5,
                BigDecimal.valueOf(10)
        );

        when(confirmPaymentUseCase.execute(new ConfirmPaymentCommand(paymentId)))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/payments/{paymentId}/confirm", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.credits").value(5))
                .andExpect(jsonPath("$.amount").value(10));

        ArgumentCaptor<ConfirmPaymentCommand> commandCaptor =
                ArgumentCaptor.forClass(ConfirmPaymentCommand.class);

        verify(confirmPaymentUseCase).execute(commandCaptor.capture());

        assertEquals(paymentId, commandCaptor.getValue().paymentId());
    }

    private record CreateCheckoutJson(
            String volunteerId,
            String idempotencyKey
    ) {
    }
}