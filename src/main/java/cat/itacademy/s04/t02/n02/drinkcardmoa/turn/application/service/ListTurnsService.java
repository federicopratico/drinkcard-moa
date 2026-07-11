package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.query.ListTurnsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.dto.result.TurnSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.in.usecase.ListTurnsUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.TurnRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.query.TurnSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListTurnsService implements ListTurnsUseCase {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final TurnRepository turnRepository;

    @Override
    public PageResult<TurnSummaryResult> execute(ListTurnsQuery query) {
        TurnSearchCriteria criteria = new TurnSearchCriteria(
                parseEmail(query.email()),
                query.date(),
                normalizePage(query.page()),
                normalizeSize(query.size())
        );

        return turnRepository.searchTurns(criteria).map(TurnSummaryResult::from);
    }

    private Email parseEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return Email.from(email);
    }

    private int normalizePage(int page) {
        return page < 0 ? DEFAULT_PAGE : page;
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
