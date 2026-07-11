package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.query;

import java.time.LocalDate;

public record ListTurnsQuery(
        String email,
        LocalDate date,
        int page,
        int size
) {
}
