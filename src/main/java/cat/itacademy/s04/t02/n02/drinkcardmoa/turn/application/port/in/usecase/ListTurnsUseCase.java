package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.usecase;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.query.ListTurnsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.result.TurnSummaryResult;

public interface ListTurnsUseCase {
    PageResult<TurnSummaryResult> execute(ListTurnsQuery query);
}
