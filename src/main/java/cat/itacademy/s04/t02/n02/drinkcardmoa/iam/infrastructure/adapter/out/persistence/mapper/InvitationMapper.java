package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.Invitation;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.InvitationStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.InvitationToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.InvitationJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.InvitationID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Component;

@Component("invitationPersistenceMapper")
public class InvitationMapper {

    public InvitationJpaEntity toEntity(Invitation invitation) {
        return InvitationJpaEntity.create(
                invitation.getId().asString(),
                invitation.getEmail().asString(),
                invitation.getRole().name(),
                invitation.getStatus().name(),
                invitation.getInvitationToken().asString()
        );
    }

    public Invitation toDomain(InvitationJpaEntity entity) {
        return Invitation.rehydrate(
                InvitationID.from(entity.getId()),
                Email.from(entity.getEmail()),
                Role.valueOf(entity.getRole()),
                InvitationToken.from(entity.getInvitationToken()),
                InvitationStatus.valueOf(entity.getStatus().toUpperCase())
        );
    }
}
