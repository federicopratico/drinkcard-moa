package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TopVolunteer {
    private final VolunteerID volunteerId;
    private final Long drinkTicketsCount;

    public TopVolunteer(UUID volunteerId, Long drinkTicketsCount) {
        this.volunteerId = VolunteerID.from(volunteerId.toString());
        this.drinkTicketsCount = drinkTicketsCount;
    }
}
