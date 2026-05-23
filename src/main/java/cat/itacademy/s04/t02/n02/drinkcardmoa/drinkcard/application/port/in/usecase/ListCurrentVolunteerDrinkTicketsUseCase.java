package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListCurrentVolunteerDrinkTicketsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkTicketSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;

public interface ListCurrentVolunteerDrinkTicketsUseCase {
    PageResult<DrinkTicketSummaryResult> execute(ListCurrentVolunteerDrinkTicketsQuery query);
}
