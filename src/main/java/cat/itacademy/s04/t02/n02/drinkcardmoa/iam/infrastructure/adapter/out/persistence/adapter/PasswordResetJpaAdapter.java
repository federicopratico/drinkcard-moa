package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.adapter;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.PasswordResetRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.PasswordReset;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.mapper.PasswordResetMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository.JpaPasswordResetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PasswordResetJpaAdapter implements PasswordResetRepository {

    private final JpaPasswordResetRepository jpaPasswordResetRepository;
    private final PasswordResetMapper mapper;

    @Override
    public PasswordReset save(PasswordReset passwordReset) {
        return mapper.toDomain(jpaPasswordResetRepository.save(mapper.toEntity(passwordReset)));
    }

    @Override
    public Optional<PasswordReset> findByPasswordResetToken(HashedToken token) {
        return jpaPasswordResetRepository.findByPasswordResetToken(token.asString())
                .map(mapper::toDomain);
    }
}
