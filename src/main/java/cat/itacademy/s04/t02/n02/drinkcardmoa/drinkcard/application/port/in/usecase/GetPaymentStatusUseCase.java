package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.PaymentStatusResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetPaymentStatusQuery;

public interface GetPaymentStatusUseCase {
    PaymentStatusResult execute(GetPaymentStatusQuery query);
}
