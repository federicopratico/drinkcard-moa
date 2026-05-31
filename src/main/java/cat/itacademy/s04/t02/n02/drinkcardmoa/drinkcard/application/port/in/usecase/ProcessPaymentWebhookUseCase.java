package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.ProcessPaymentWebhookCommand;

public interface ProcessPaymentWebhookUseCase {
    void execute(ProcessPaymentWebhookCommand cmd);
}
