package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidEmailException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.command.AddTurnCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.result.AddTurnResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.TurnRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.exception.TurnAlreadyExistsException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.aggregate.Turn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddTurnServiceTest {

    @Mock
    private TurnRepository turnRepository;

    @InjectMocks
    private AddTurnService service;

    @Test
    void execute_WhenTurnDoesNotExist_CreatesTurnAndReturnsResult() {
        String email = "jane@example.com";
        LocalDate date = LocalDate.of(2026, 7, 17);

        when(turnRepository.existsByEmailAndDate(any(Email.class), eq(date))).thenReturn(false);
        when(turnRepository.save(any(Turn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddTurnResult result = service.execute(new AddTurnCommand(email, date));

        ArgumentCaptor<Turn> turnCaptor = ArgumentCaptor.forClass(Turn.class);
        verify(turnRepository).save(turnCaptor.capture());
        Turn savedTurn = turnCaptor.getValue();

        assertAll(
                () -> assertNotNull(result.turnId()),
                () -> assertEquals(email, result.email()),
                () -> assertEquals(date, result.date()),
                () -> assertNotNull(result.createdAt()),
                () -> assertEquals(email, savedTurn.getEmail().asString()),
                () -> assertEquals(date, savedTurn.getDate()),
                () -> assertNotNull(savedTurn.getTurnId()),
                () -> assertNotNull(savedTurn.getCreatedAt())
        );
    }

    @Test
    void execute_WhenEmailHasUpperCase_NormalizesToLowerCase() {
        String email = "Jane@Example.COM";
        LocalDate date = LocalDate.of(2026, 7, 17);

        when(turnRepository.existsByEmailAndDate(any(Email.class), eq(date))).thenReturn(false);
        when(turnRepository.save(any(Turn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddTurnResult result = service.execute(new AddTurnCommand(email, date));

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(turnRepository).existsByEmailAndDate(emailCaptor.capture(), eq(date));

        assertAll(
                () -> assertEquals("jane@example.com", emailCaptor.getValue().asString()),
                () -> assertEquals("jane@example.com", result.email())
        );
    }

    @Test
    void execute_WhenTurnAlreadyExists_ThrowsAndDoesNotSave() {
        String email = "jane@example.com";
        LocalDate date = LocalDate.of(2026, 7, 17);

        when(turnRepository.existsByEmailAndDate(any(Email.class), eq(date))).thenReturn(true);

        assertThrows(
                TurnAlreadyExistsException.class,
                () -> service.execute(new AddTurnCommand(email, date))
        );

        verify(turnRepository, never()).save(any(Turn.class));
    }

    @Test
    void execute_WhenEmailIsInvalid_ThrowsInvalidEmailAndDoesNotUseRepository() {
        assertThrows(
                InvalidEmailException.class,
                () -> service.execute(new AddTurnCommand("not-an-email", LocalDate.of(2026, 7, 17)))
        );

        verifyNoInteractions(turnRepository);
    }
}
