package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.query;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;

import java.time.LocalDate;

public record TurnSearchCriteria(
        Email email,
        LocalDate date,
        int page,
        int size
) {
}
