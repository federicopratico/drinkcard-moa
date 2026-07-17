package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.dto;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;

public record VolunteerProfile(
        VolunteerID id,
        String firstName,
        String lastName,
        String email
) {
}
