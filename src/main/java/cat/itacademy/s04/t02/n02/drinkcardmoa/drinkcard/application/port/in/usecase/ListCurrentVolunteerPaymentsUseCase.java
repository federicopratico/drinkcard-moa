package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListCurrentVolunteerPaymentsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;

public interface ListCurrentVolunteerPaymentsUseCase {
    PageResult<PaymentSummaryResult> execute(ListCurrentVolunteerPaymentsQuery query);
}
