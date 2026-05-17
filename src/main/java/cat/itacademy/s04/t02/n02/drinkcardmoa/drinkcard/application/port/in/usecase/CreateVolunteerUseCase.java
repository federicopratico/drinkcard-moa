package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreateVolunteerCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateVolunteerResult;

public interface CreateVolunteerUseCase {
    CreateVolunteerResult execute(CreateVolunteerCommand cmd);
}
