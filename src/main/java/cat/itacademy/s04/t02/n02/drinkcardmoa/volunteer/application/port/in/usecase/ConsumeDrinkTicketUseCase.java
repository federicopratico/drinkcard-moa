package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.ConsumeDrinkTicketCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.ConsumeDrinkTicketResult;

public interface ConsumeDrinkTicketUseCase {
    ConsumeDrinkTicketResult execute(ConsumeDrinkTicketCommand cmd);
}
