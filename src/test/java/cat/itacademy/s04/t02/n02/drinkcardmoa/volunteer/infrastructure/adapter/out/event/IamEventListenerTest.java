package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.out.event;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.UserRegisteredEvent;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.command.CreateVolunteerCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.dto.result.CreateVolunteerResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.in.usecase.CreateVolunteerUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IamEventListenerTest {

    @Mock
    private CreateVolunteerUseCase createVolunteerUseCase;

    @InjectMocks
    private IamEventListener iamEventListener;

    @Test
    void onUserRegistered_WhenEventIsValid_CreateVolunteer() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                "volunteer-id",
                "volunteer@email.com",
                "VOLUNTEER",
                Instant.now()
        );

        when(createVolunteerUseCase.execute(org.mockito.ArgumentMatchers.any(CreateVolunteerCommand.class)))
                .thenReturn(new CreateVolunteerResult("volunteer-id", 0));

        iamEventListener.onUserRegistered(event);

        ArgumentCaptor<CreateVolunteerCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateVolunteerCommand.class);

        verify(createVolunteerUseCase, times(1)).execute(commandCaptor.capture());

        CreateVolunteerCommand capturedCommand = commandCaptor.getValue();

        assertEquals(event.volunteerId(), capturedCommand.volunteerId());
    }

    @Test
    void onUserRegistered_WhenUseCaseFails_RethrowException() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                "volunteer-id",
                "volunteer@email.com",
                "VOLUNTEER",
                Instant.now()
        );

        RuntimeException exception = new RuntimeException("Could not create volunteer");

        when(createVolunteerUseCase.execute(org.mockito.ArgumentMatchers.any(CreateVolunteerCommand.class)))
                .thenThrow(exception);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            iamEventListener.onUserRegistered(event);
        });

        assertEquals(exception, thrown);

        verify(createVolunteerUseCase, times(1))
                .execute(org.mockito.ArgumentMatchers.any(CreateVolunteerCommand.class));
    }
}
