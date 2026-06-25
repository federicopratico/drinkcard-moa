package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.PasswordReset;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.PasswordResetStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.PasswordResetJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.PasswordResetID;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetMapper {

    public PasswordReset toDomain(PasswordResetJpaEntity entity) {
        return PasswordReset.rehydrate(
                PasswordResetID.from(entity.getId().toString()),
                Email.from(entity.getEmail()),
                HashedToken.from(entity.getPasswordResetToken()),
                PasswordResetStatus.valueOf(entity.getStatus().toUpperCase()),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getUsedAt()
        );
    }

    public PasswordResetJpaEntity toEntity(PasswordReset domain) {
        return PasswordResetJpaEntity.create(
                domain.getPasswordResetId().value(),
                domain.getEmail().asString(),
                domain.getToken().asString(),
                domain.getStatus().name(),
                domain.getCreatedAt(),
                domain.getExpiresAt(),
                domain.getUsedAt()
        );
    }
}
