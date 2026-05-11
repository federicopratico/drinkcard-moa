package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.CreateVolunteerCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.CreateVolunteerResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase.CreateVolunteerUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.VolunteerRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Volunteer;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Transactional
public class CreateVolunteerService implements CreateVolunteerUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateVolunteerService.class);

    private final VolunteerRepository volunteerRepository;

    public CreateVolunteerService(VolunteerRepository volunteerRepository) {
        this.volunteerRepository = volunteerRepository;
    }

    @Override
    public CreateVolunteerResult execute(CreateVolunteerCommand cmd) {
        log.info("Creating volunteer for volunteerId: {}", cmd.volunteerId());

        VolunteerID volunteerId = VolunteerID.from(cmd.volunteerId());

        if (volunteerRepository.existsByVolunteerId(volunteerId)) {
            log.warn("Volunteer already exists with volunteerId: {}", cmd.volunteerId());

            Volunteer existingVolunteer = volunteerRepository
                    .findByVolunteerId(volunteerId)
                    .orElseThrow();

            return CreateVolunteerResult.from(existingVolunteer);
        }

        Volunteer volunteer = Volunteer.create(volunteerId);

        Volunteer savedVolunteer = volunteerRepository.save(volunteer);

        log.info("Successfully created volunteer: {}", savedVolunteer.getVolunteerID().asString());

        return CreateVolunteerResult.from(savedVolunteer);
    }
}
