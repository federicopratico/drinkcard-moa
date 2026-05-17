package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkCardAccountJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class DrinkCardAccountMapper {

    public DrinkCardAccountJpaEntity toEntity(DrinkCardAccount drinkCardAccount) {
        DrinkCardAccountJpaEntity entity = DrinkCardAccountJpaEntity.create(
                drinkCardAccount.getVolunteerId().asString(),
                drinkCardAccount.getCredits(),
                drinkCardAccount.getLastPurchaseTimestamp(),
                drinkCardAccount.getCreatedAt()
        );

        if (drinkCardAccount.getId() != null) {
            entity.setId(drinkCardAccount.getId());
        }

        return entity;
    }

    public DrinkCardAccount toDomain(DrinkCardAccountJpaEntity entity) {
        return DrinkCardAccount.rehydrate(
                entity.getId(),
                VolunteerID.from(entity.getVolunteerId()),
                entity.getCredits(),
                entity.getLastPurchaseTimestamp(),
                entity.getCreatedAt()
        );
    }
}
