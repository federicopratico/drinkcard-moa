package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Volunteer;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.out.persistence.entity.VolunteerJpaEntity;

public class VolunteerMapper {

    public VolunteerJpaEntity toEntity(Volunteer volunteer) {
        return VolunteerJpaEntity.create(
                volunteer.getVolunteerID().asString(),
                volunteer.getCredits(),
                volunteer.getLastPurchaseTimestamp(),
                volunteer.getCreatedAt()
        );
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
