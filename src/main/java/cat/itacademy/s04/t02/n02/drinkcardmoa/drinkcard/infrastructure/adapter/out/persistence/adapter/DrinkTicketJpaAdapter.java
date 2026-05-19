package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.adapter;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicketID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicketStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.mapper.DrinkTicketMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository.JpaDrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import lombok.AllArgsConstructor;
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
}
