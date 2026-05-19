package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;

import java.time.Instant;

public record PaymentSearchCriteria(
        VolunteerID volunteerId,
        PaymentStatus status,
        Instant from,
        Instant to,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
}
