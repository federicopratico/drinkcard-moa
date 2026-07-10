package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.AddDrinkCardCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListPaymentsAdminQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.AddDrinkCardUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListPaymentsAdminUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.request.AddDrinkCardRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.AddDrinkCardResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.dto.response.PaymentSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper.AdminPaymentControllerMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin/payments")
@AllArgsConstructor
public class AdminPaymentController {

    private final ListPaymentsAdminUseCase listPaymentsAdminUseCase;
    private final AddDrinkCardUseCase addDrinkCardUseCase;
    private final AdminPaymentControllerMapper mapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<PaymentSummaryResponse>> listPayments(
            @RequestParam(required = false) String volunteerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        ListPaymentsAdminQuery query = new ListPaymentsAdminQuery(
                volunteerId,
                status,
                from,
                to,
                page,
                size,
                sort
        );

        PageResult<PaymentSummaryResult> result = listPaymentsAdminUseCase.execute(query);

        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @PostMapping("/add-drink-card")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddDrinkCardResponse> addDrinkCardManually (@Valid @RequestBody AddDrinkCardRequest request) {
        AddDrinkCardResponse result = mapper.toResponse(addDrinkCardUseCase.execute(new AddDrinkCardCommand(request.volunteerId())));
        return ResponseEntity.ok(result);
    }
}
