package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.ConfirmPaymentCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.ConfirmPaymentResult;

public interface ConfirmPaymentUseCase {
    ConfirmPaymentResult execute(ConfirmPaymentCommand cmd);
}
