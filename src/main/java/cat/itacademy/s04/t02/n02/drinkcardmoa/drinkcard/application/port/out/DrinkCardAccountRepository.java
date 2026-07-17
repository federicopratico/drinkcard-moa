package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.DrinkCardAccountSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;

import java.util.Optional;

public interface DrinkCardAccountRepository {
    DrinkCardAccount save(DrinkCardAccount drinkCardAccount);
    Optional<DrinkCardAccount> findByVolunteerId(VolunteerID volunteerID);
    boolean existsByVolunteerId(VolunteerID volunteerID);
    PageResult<DrinkCardAccount> searchDrinkCardAccounts(DrinkCardAccountSearchCriteria criteria);
    long sumAvailableCredits();
    long countActiveCards();
}
