package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.event;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.event.UserRegisteredEvent;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreateDrinkCardAccountCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreateDrinkCardAccountResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.CreateDrinkCardAccountUseCase;
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
    private CreateDrinkCardAccountUseCase createDrinkCardAccountUseCase;

    @InjectMocks
    private IamEventListener iamEventListener;

    @Test
    void onUserRegistered_WhenEventIsValid_CreateDrinkCardAccount() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                "volunteer-id",
                "volunteer@userId.com",
                "VOLUNTEER",
                Instant.now()
        );

        when(createDrinkCardAccountUseCase.execute(org.mockito.ArgumentMatchers.any(CreateDrinkCardAccountCommand.class)))
                .thenReturn(new CreateDrinkCardAccountResult("volunteer-id", 0));

        iamEventListener.onUserRegistered(event);

        ArgumentCaptor<CreateDrinkCardAccountCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateDrinkCardAccountCommand.class);

        verify(createDrinkCardAccountUseCase, times(1)).execute(commandCaptor.capture());

        CreateDrinkCardAccountCommand capturedCommand = commandCaptor.getValue();

        assertEquals(event.volunteerId(), capturedCommand.volunteerId());
    }

    @Test
    void onUserRegistered_WhenUseCaseFails_RethrowException() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                "volunteer-id",
                "volunteer@userId.com",
                "VOLUNTEER",
                Instant.now()
        );

        RuntimeException exception = new RuntimeException("Could not create drink card account");

        when(createDrinkCardAccountUseCase.execute(org.mockito.ArgumentMatchers.any(CreateDrinkCardAccountCommand.class)))
                .thenThrow(exception);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            iamEventListener.onUserRegistered(event);
        });

        assertEquals(exception, thrown);

        verify(createDrinkCardAccountUseCase, times(1))
                .execute(org.mockito.ArgumentMatchers.any(CreateDrinkCardAccountCommand.class));
    }
}
