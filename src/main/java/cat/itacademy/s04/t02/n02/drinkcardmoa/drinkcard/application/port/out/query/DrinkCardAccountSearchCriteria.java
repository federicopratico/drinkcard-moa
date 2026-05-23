package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkCardAccountStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;

public record DrinkCardAccountSearchCriteria(
        VolunteerID volunteerId,
        DrinkCardAccountStatus status,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
}
