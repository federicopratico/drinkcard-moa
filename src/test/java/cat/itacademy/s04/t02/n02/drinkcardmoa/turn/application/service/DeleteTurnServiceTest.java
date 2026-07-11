package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.command.DeleteTurnCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.TurnRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.exception.TurnNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.aggregate.Turn;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.valueobject.TurnID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteTurnServiceTest {

    @Mock
    private TurnRepository turnRepository;

    @InjectMocks
    private DeleteTurnService service;

    @Test
    void execute_WhenTurnExists_DeletesTurn() {
        TurnID turnId = TurnID.generate();
        Turn turn = Turn.rehydrate(
                turnId,
                Email.from("jane@example.com"),
                LocalDate.of(2026, 7, 17),
                Instant.now()
        );
        when(turnRepository.findById(turnId)).thenReturn(Optional.of(turn));

        service.execute(new DeleteTurnCommand(turnId.asString()));

        verify(turnRepository).deleteById(turnId);
    }

    @Test
    void execute_WhenTurnDoesNotExist_ThrowsTurnNotFoundAndDoesNotDelete() {
        TurnID turnId = TurnID.generate();
        when(turnRepository.findById(turnId)).thenReturn(Optional.empty());

        assertThrows(
                TurnNotFoundException.class,
                () -> service.execute(new DeleteTurnCommand(turnId.asString()))
        );

        verify(turnRepository, never()).deleteById(any());
    }

    @Test
    void execute_WhenTurnIdIsInvalidUuid_ThrowsTurnNotFoundAndDoesNotQueryOrDelete() {
        assertThrows(
                TurnNotFoundException.class,
                () -> service.execute(new DeleteTurnCommand("not-a-uuid"))
        );

        verify(turnRepository, never()).findById(any());
        verify(turnRepository, never()).deleteById(any());
    }
}
