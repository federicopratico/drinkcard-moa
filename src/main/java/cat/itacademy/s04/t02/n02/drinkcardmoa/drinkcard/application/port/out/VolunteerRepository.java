package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.Volunteer;

import java.util.Optional;

public interface VolunteerRepository {
    Volunteer save(Volunteer volunteer);
    Optional<Volunteer> findByVolunteerId(VolunteerID volunteerID);
    boolean existsByVolunteerId(VolunteerID volunteerID);
}
