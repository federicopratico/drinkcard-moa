package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.result;

import java.time.Instant;
import java.time.LocalDate;

public record AddTurnResult(
        String turnId,
        String email,
        LocalDate date,
        Instant createdAt
) {
}
