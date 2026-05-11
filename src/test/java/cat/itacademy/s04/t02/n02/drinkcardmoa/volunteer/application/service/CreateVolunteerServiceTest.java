package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.CreateVolunteerCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.CreateVolunteerResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.VolunteerRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Volunteer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateVolunteerServiceTest {

    @Mock
    private VolunteerRepository volunteerRepository;

    @InjectMocks
    private CreateVolunteerService volunteerService;

    @Test
    void execute_WhenVolunteerDoesNotExist_CreateNewVolunteer() {

        String volunteerId = VolunteerID.generate().asString();
        CreateVolunteerCommand cmd = new CreateVolunteerCommand(volunteerId);

        when(volunteerRepository.save(any(Volunteer.class))).thenAnswer(i -> i.getArguments()[0]);

        CreateVolunteerResult result = volunteerService.execute(cmd);

        assertNotNull(result);
        assertEquals(volunteerId, result.volunteerID());
        assertEquals(0, result.credits());

        verify(volunteerRepository, times(1)).save(any(Volunteer.class));
    }
}