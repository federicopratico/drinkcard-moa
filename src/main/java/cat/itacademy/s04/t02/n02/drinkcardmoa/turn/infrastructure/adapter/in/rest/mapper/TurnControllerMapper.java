package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.in.rest.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.command.AddTurnCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.result.AddTurnResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.result.TurnSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.in.rest.dto.request.AddTurnRequest;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.in.rest.dto.response.TurnResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.in.rest.dto.response.TurnSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TurnControllerMapper {

    public AddTurnCommand toCommand(AddTurnRequest request) {
        return new AddTurnCommand(request.email(), request.date());
    }

    public TurnResponse toResponse(AddTurnResult result) {
        return new TurnResponse(
                result.turnId(),
                result.email(),
                result.date(),
                result.createdAt()
        );
    }

    public TurnSummaryResponse toResponse(TurnSummaryResult result) {
        return new TurnSummaryResponse(
                result.turnId(),
                result.email(),
                result.date(),
                result.createdAt()
        );
    }

    public PageResponse<TurnSummaryResponse> toResponse(PageResult<TurnSummaryResult> result) {
        List<TurnSummaryResponse> content = result.content()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}
