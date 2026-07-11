package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.query.TurnSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.aggregate.Turn;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.valueobject.TurnID;

import java.time.LocalDate;
import java.util.Optional;

public interface TurnRepository {
    Turn save(Turn turn);
    boolean existsByEmailAndDate(Email email, LocalDate date);
    Optional<Turn> findById(TurnID turnId);
    void deleteById(TurnID turnId);
    PageResult<Turn> searchTurns(TurnSearchCriteria criteria);
}
