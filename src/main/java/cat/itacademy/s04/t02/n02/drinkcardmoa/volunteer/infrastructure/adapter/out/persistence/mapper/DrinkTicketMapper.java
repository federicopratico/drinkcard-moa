package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.DrinkTicketID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.DrinkTicketStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.DrinkType;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.out.persistence.entity.DrinkTicketJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class DrinkTicketMapper {

    public DrinkTicket toDomain(DrinkTicketJpaEntity entity) {
        return DrinkTicket.rehydrate(
                DrinkTicketID.from(entity.getDrinkTicketId().toString()),
                VolunteerID.from(entity.getVolunteerId().toString()),
                DrinkType.valueOf(entity.getDrinkType().toUpperCase()),
                DrinkTicketStatus.valueOf(entity.getStatus().toUpperCase()),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getConsumedAt(),
                entity.getConsumedByStaffId()
        );
    }

    public DrinkTicketJpaEntity toEntity(DrinkTicket drinkTicket) {
        return DrinkTicketJpaEntity.create(
                drinkTicket.getDrinkTicketId().value(),
                drinkTicket.getVolunteerId().value(),
                drinkTicket.getDrinkType().toString(),
                drinkTicket.getStatus().toString(),
                drinkTicket.getCreatedAt(),
                drinkTicket.getExpiresAt(),
                drinkTicket.getConsumedAt(),
                drinkTicket.getConsumedByStaffId()
        );
    }
}
