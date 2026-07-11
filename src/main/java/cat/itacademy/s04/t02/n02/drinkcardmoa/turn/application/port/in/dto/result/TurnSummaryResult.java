package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.aggregate.Turn;

import java.time.Instant;
import java.time.LocalDate;

public record TurnSummaryResult(
        String turnId,
        String email,
        LocalDate date,
        Instant createdAt
) {
    public static TurnSummaryResult from(Turn turn) {
        return new TurnSummaryResult(
                turn.getTurnId().asString(),
                turn.getEmail().asString(),
                turn.getDate(),
                turn.getCreatedAt()
        );
    }
}
