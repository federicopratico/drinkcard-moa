package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Volunteer;

public record CreateVolunteerResult(
        String volunteerID,
        int credits
)
{
    public static CreateVolunteerResult from(Volunteer volunteer) {
        return new CreateVolunteerResult(
                volunteer.getVolunteerID().asString(),
                volunteer.getCredits()
        );
    }
}
