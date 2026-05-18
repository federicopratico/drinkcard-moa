package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccount;

import java.util.List;
import java.util.Optional;

public interface DrinkCardAccountRepository {
    DrinkCardAccount save(DrinkCardAccount drinkCardAccount);
    Optional<DrinkCardAccount> findByVolunteerId(VolunteerID volunteerID);
    boolean existsByVolunteerId(VolunteerID volunteerID);
    List<DrinkCardAccount> findAll();
}
