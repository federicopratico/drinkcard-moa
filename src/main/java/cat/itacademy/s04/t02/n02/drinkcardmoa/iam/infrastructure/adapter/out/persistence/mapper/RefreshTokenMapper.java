package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.RefreshToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenFamilyID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapper {

    public RefreshTokenJpaEntity toEntity(RefreshToken refreshToken) {
        return RefreshTokenJpaEntity.create(
                refreshToken.getId().value(),
                refreshToken.getUserId().asString(),
                refreshToken.getTokenHash().asString(),
                refreshToken.getFamilyId().value(),
                refreshToken.getCreatedAt(),
                refreshToken.getExpiresAt(),
                refreshToken.getLastUsedAt(),
                refreshToken.getRevokedAt(),
                refreshToken.getReplacedBy() == null ? null : refreshToken.getReplacedBy().value()
        );
    }

    public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.rehydrate(
                RefreshTokenID.from(entity.getId().toString()),
                VolunteerID.from(entity.getUserId()),
                HashedToken.from(entity.getTokenHash()),
                RefreshTokenFamilyID.from(entity.getFamilyId().toString()),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getLastUsedAt(),
                entity.getRevokedAt(),
                entity.getReplacedBy() != null ? RefreshTokenID.from(entity.getReplacedBy().toString()) : null
        );
    }
}
