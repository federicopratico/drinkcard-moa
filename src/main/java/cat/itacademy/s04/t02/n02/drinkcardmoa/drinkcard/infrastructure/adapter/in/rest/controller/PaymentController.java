package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.ConfirmPaymentResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreatePaymentCheckoutResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ConfirmPaymentUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.CreatePaymentCheckoutUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.CreatePaymentCheckoutRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.ConfirmPaymentResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.CreatePaymentCheckoutResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper.PaymentControllerMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.config.PaymentProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final CreatePaymentCheckoutUseCase createPaymentCheckoutUseCase;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final PaymentControllerMapper mapper;
    private final PaymentProperties paymentProperties;

    public PaymentController(CreatePaymentCheckoutUseCase createPaymentCheckoutUseCase,
                             ConfirmPaymentUseCase confirmPaymentUseCase,
                             PaymentControllerMapper mapper,
                             PaymentProperties paymentProperties) {
        this.createPaymentCheckoutUseCase = createPaymentCheckoutUseCase;
        this.confirmPaymentUseCase = confirmPaymentUseCase;
        this.mapper = mapper;
        this.paymentProperties = paymentProperties;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CreatePaymentCheckoutResponse> createCheckout(@RequestBody CreatePaymentCheckoutRequest request) {

        CreatePaymentCheckoutResult result = createPaymentCheckoutUseCase.execute(mapper.toCommand(request, paymentProperties.getFrontendSuccessUrl()));

        return ResponseEntity.status(201).body(mapper.toResponse(result));
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<ConfirmPaymentResponse> confirmPayment(@PathVariable String paymentId) {

        ConfirmPaymentResult result = confirmPaymentUseCase.execute(mapper.toCommand(paymentId));

        return ResponseEntity.status(200).body(mapper.toResponse(result));
    }

}
