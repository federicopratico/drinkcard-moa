package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.CreatePaymentCheckoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.CreatePaymentCheckoutResult;

public interface CreatePaymentCheckoutUseCase {
    CreatePaymentCheckoutResult execute(CreatePaymentCheckoutCommand cmd);
}
