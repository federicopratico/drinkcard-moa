package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListPaymentsAdminQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.AdminPaymentSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;

public interface ListPaymentsAdminUseCase {
    PageResult<AdminPaymentSummaryResult> execute(ListPaymentsAdminQuery query);
}
