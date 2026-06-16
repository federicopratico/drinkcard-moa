package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.adapter;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.RefreshToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.RefreshTokenFamilyID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.mapper.RefreshTokenMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository.JpaRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenJpaAdapter implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;
    private final RefreshTokenMapper mapper;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return mapper.toDomain(jpaRefreshTokenRepository.save(mapper.toEntity(refreshToken)));
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(HashedToken tokenHash) {
        return jpaRefreshTokenRepository.findByTokenHash(tokenHash.asString())
                .map(mapper::toDomain);
    }

    @Override
    public void revokeFamily(RefreshTokenFamilyID refreshTokenFamilyID, Instant revokedAt) {
        jpaRefreshTokenRepository.revokeFamily(refreshTokenFamilyID.value(), revokedAt);
    }
}
