package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.out.persistence.adapter;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.out.persistence.JpaSpecificationBuilder;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.TurnRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.port.out.query.TurnSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.aggregate.Turn;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.valueobject.TurnID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.out.persistence.entity.TurnJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.out.persistence.mapper.TurnMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.out.persistence.repository.JpaTurnRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class TurnJpaAdapter implements TurnRepository {

    private final JpaTurnRepository jpaTurnRepository;
    private final TurnMapper mapper;

    @Override
    public Turn save(Turn turn) {
        TurnJpaEntity entity = jpaTurnRepository.save(mapper.toEntity(turn));
        return mapper.toDomain(entity);
    }

    @Override
    public boolean existsByEmailAndDate(Email email, LocalDate date) {
        return jpaTurnRepository.existsByEmailAndTurnDate(email.asString(), date);
    }

    @Override
    public Optional<Turn> findById(TurnID turnId) {
        return jpaTurnRepository.findById(turnId.value()).map(mapper::toDomain);
    }

    @Override
    public void deleteById(TurnID turnId) {
        jpaTurnRepository.deleteById(turnId.value());
    }

    @Override
    public PageResult<Turn> searchTurns(TurnSearchCriteria criteria) {
        PageRequest pageRequest = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by(Sort.Order.asc("turnDate"), Sort.Order.asc("email"))
        );

        Page<TurnJpaEntity> page = jpaTurnRepository.findAll(
                toSpecification(criteria),
                pageRequest
        );

        List<Turn> turns = page.getContent()
                .stream()
                .map(mapper::toDomain)
                .toList();

        return new PageResult<>(
                turns,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private Specification<TurnJpaEntity> toSpecification(TurnSearchCriteria criteria) {
        return JpaSpecificationBuilder.<TurnJpaEntity>builder()
                .equal("email", criteria.email() == null ? null : criteria.email().asString())
                .equal("turnDate", criteria.date())
                .build();
    }
}
