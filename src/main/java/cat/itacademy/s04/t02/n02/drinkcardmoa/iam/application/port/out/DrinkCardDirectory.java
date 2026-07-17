package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;

import java.util.Collection;
import java.util.Map;

public interface DrinkCardDirectory {
    Map<String, DrinkCardAccountSummaryResult> findAllByVolunteerIds(Collection<VolunteerID> ids);

    DrinkCardAccountSummaryResult findByVolunteerId(VolunteerID id);
}
