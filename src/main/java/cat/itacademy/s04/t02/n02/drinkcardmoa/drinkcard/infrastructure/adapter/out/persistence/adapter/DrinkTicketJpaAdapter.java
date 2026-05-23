package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.adapter;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.DrinkTicketSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkTicketID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkTicketStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkTicketJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.mapper.DrinkTicketMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository.JpaDrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.out.persistence.JpaSpecificationBuilder;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class DrinkTicketJpaAdapter implements DrinkTicketRepository {

    private final JpaDrinkTicketRepository jpaDrinkTicketRepository;
    private final DrinkTicketMapper mapper;

    @Override
    public DrinkTicket save(DrinkTicket drinkTicket) {
        return mapper.toDomain(jpaDrinkTicketRepository.save(mapper.toEntity(drinkTicket)));
    }

    @Override
    public Optional<DrinkTicket> findByDrinkTicketId(DrinkTicketID drinkTicketId) {
        return jpaDrinkTicketRepository.findByDrinkTicketId(drinkTicketId.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsActivePendingByVolunteerId(VolunteerID volunteerId, Instant now) {
        return jpaDrinkTicketRepository.existsByVolunteerIdAndStatusAndExpiresAtAfter(
                volunteerId.value(),
                DrinkTicketStatus.PENDING.name(),
                now
        );
    }

    @Override
    public List<DrinkTicket> findExpiredPendingTickets(Instant now) {
        return jpaDrinkTicketRepository.findByStatusAndExpiresAtLessThanEqual(DrinkTicketStatus.PENDING.name(), now)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public PageResult<DrinkTicket> searchAdminDrinkTickets(DrinkTicketSearchCriteria criteria) {
        return searchDrinkTickets(criteria);
    }

    @Override
    public PageResult<DrinkTicket> searchVolunteerDrinkTickets(DrinkTicketSearchCriteria criteria) {
        return searchDrinkTickets(criteria);
    }

    private PageResult<DrinkTicket> searchDrinkTickets(DrinkTicketSearchCriteria criteria) {
        Sort.Direction direction = Sort.Direction.fromString(criteria.sortDirection());
        PageRequest pageRequest = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by(direction, criteria.sortBy())
        );

        Page<DrinkTicketJpaEntity> page = jpaDrinkTicketRepository.findAll(toSpecification(criteria), pageRequest);
        List<DrinkTicket> drinkTickets = page.getContent()
                .stream()
                .map(mapper::toDomain)
                .toList();

        return new PageResult<>(
                drinkTickets,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
                );
    }

    private Specification<DrinkTicketJpaEntity> toSpecification(DrinkTicketSearchCriteria criteria) {
        return JpaSpecificationBuilder.<DrinkTicketJpaEntity>builder()
                .equal("volunteerId", criteria.volunteerId() == null ? null : criteria.volunteerId().value())
                .equal("status", criteria.status() == null ? null : criteria.status().name())
                .greaterThanOrEqualTo("createdAt", criteria.from())
                .lessThanOrEqualTo("createdAt", criteria.to())
                .build();
    }
}
