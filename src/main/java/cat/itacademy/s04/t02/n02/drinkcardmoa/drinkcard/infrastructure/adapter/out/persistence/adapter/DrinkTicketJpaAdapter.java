package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.adapter;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkTicketID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.mapper.DrinkTicketMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository.JpaDrinkTicketRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
