package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.dto.VolunteerProfile;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;

import java.util.Collection;
import java.util.Map;

/**
 * Anti-corruption port exposing volunteer identity data from the IAM context
 * to the drinkcard context, without leaking the {@code User} aggregate.
 */
public interface VolunteerDirectory {

    /**
     * Returns a map keyed by {@link VolunteerID} for every id resolvable in the directory.
     * Unknown ids are simply absent from the map.
     */
    Map<VolunteerID, VolunteerProfile> findAllByIds(Collection<VolunteerID> ids);
}
