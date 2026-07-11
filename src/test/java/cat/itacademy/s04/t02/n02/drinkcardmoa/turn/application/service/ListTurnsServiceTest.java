package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidEmailException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.query.ListTurnsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.result.TurnSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.TurnRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.query.TurnSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.aggregate.Turn;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.valueobject.TurnID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListTurnsServiceTest {

    @Mock
    private TurnRepository turnRepository;

    @InjectMocks
    private ListTurnsService service;

    @Test
    void execute_WhenNoFilters_PassesNullFiltersAndDefaultPageSizeToRepository() {
        when(turnRepository.searchTurns(any(TurnSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));

        PageResult<TurnSummaryResult> result = service.execute(new ListTurnsQuery(null, null, 0, 0));

        ArgumentCaptor<TurnSearchCriteria> captor = ArgumentCaptor.forClass(TurnSearchCriteria.class);
        verify(turnRepository).searchTurns(captor.capture());
        TurnSearchCriteria criteria = captor.getValue();

        assertAll(
                () -> assertNull(criteria.email()),
                () -> assertNull(criteria.date()),
                () -> assertEquals(0, criteria.page()),
                () -> assertEquals(20, criteria.size()),
                () -> assertEquals(0, result.totalElements())
        );
    }

    @Test
    void execute_WhenFiltersProvided_NormalizesEmailAndPassesDateThrough() {
        when(turnRepository.searchTurns(any(TurnSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));

        service.execute(new ListTurnsQuery("Jane@Example.COM", LocalDate.of(2026, 7, 17), 2, 50));

        ArgumentCaptor<TurnSearchCriteria> captor = ArgumentCaptor.forClass(TurnSearchCriteria.class);
        verify(turnRepository).searchTurns(captor.capture());
        TurnSearchCriteria criteria = captor.getValue();

        assertAll(
                () -> assertEquals("jane@example.com", criteria.email().asString()),
                () -> assertEquals(LocalDate.of(2026, 7, 17), criteria.date()),
                () -> assertEquals(2, criteria.page()),
                () -> assertEquals(50, criteria.size())
        );
    }

    @Test
    void execute_WhenSizeExceedsMax_CapsAt100() {
        when(turnRepository.searchTurns(any(TurnSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(), 0, 100, 0, 0));

        service.execute(new ListTurnsQuery(null, null, 0, 500));

        ArgumentCaptor<TurnSearchCriteria> captor = ArgumentCaptor.forClass(TurnSearchCriteria.class);
        verify(turnRepository).searchTurns(captor.capture());
        assertEquals(100, captor.getValue().size());
    }

    @Test
    void execute_WhenPageIsNegative_FallsBackToZero() {
        when(turnRepository.searchTurns(any(TurnSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));

        service.execute(new ListTurnsQuery(null, null, -5, 20));

        ArgumentCaptor<TurnSearchCriteria> captor = ArgumentCaptor.forClass(TurnSearchCriteria.class);
        verify(turnRepository).searchTurns(captor.capture());
        assertEquals(0, captor.getValue().page());
    }

    @Test
    void execute_WhenRepositoryReturnsTurns_MapsToSummaries() {
        Turn turn = Turn.rehydrate(
                TurnID.from("11111111-1111-1111-1111-111111111111"),
                Email.from("jane@example.com"),
                LocalDate.of(2026, 7, 17),
                Instant.parse("2026-05-18T10:00:00Z")
        );
        when(turnRepository.searchTurns(any(TurnSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(turn), 0, 20, 1, 1));

        PageResult<TurnSummaryResult> result = service.execute(new ListTurnsQuery(null, null, 0, 20));

        assertAll(
                () -> assertEquals(1, result.content().size()),
                () -> assertEquals("11111111-1111-1111-1111-111111111111", result.content().getFirst().turnId()),
                () -> assertEquals("jane@example.com", result.content().getFirst().email()),
                () -> assertEquals(LocalDate.of(2026, 7, 17), result.content().getFirst().date()),
                () -> assertEquals(Instant.parse("2026-05-18T10:00:00Z"), result.content().getFirst().createdAt())
        );
    }

    @Test
    void execute_WhenEmailFilterIsInvalid_ThrowsAndDoesNotHitRepository() {
        assertThrows(
                InvalidEmailException.class,
                () -> service.execute(new ListTurnsQuery("not-an-email", null, 0, 20))
        );

        verifyNoInteractions(turnRepository);
    }
}
