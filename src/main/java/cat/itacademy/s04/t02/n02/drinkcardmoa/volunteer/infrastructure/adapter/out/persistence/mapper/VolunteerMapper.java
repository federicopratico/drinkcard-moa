package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Volunteer;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.out.persistence.entity.VolunteerJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class VolunteerMapper {

    public VolunteerJpaEntity toEntity(Volunteer volunteer) {
        VolunteerJpaEntity entity = VolunteerJpaEntity.create(
                volunteer.getVolunteerID().asString(),
                volunteer.getCredits(),
                volunteer.getLastPurchaseTimestamp(),
                volunteer.getCreatedAt()
        );

        if (volunteer.getId() != null) {
            entity.setId(volunteer.getId());
        }

        return entity;
    }

    public Volunteer toDomain(VolunteerJpaEntity entity) {
        return Volunteer.rehydrate(
                entity.getId(),
                VolunteerID.from(entity.getVolunteerId()),
                entity.getCredits(),
                entity.getLastPurchaseTimestamp(),
                entity.getCreatedAt()
        );
    }
}
