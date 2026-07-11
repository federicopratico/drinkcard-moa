package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.aggregate.Turn;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.domain.model.valueobject.TurnID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.out.persistence.entity.TurnJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class TurnMapper {

    public TurnJpaEntity toEntity(Turn turn) {
        return TurnJpaEntity.create(
                turn.getTurnId().value(),
                turn.getEmail().asString(),
                turn.getDate(),
                turn.getCreatedAt()
        );
    }

    public Turn toDomain(TurnJpaEntity entity) {
        return Turn.rehydrate(
                new TurnID(entity.getTurnId()),
                Email.from(entity.getEmail()),
                entity.getTurnDate(),
                entity.getCreatedAt()
        );
    }
}
