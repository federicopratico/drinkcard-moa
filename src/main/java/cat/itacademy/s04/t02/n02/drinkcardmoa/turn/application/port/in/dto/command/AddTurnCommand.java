package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.command;

import java.time.LocalDate;

public record AddTurnCommand(
        String email,
        LocalDate date
) {
}
