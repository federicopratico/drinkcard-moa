package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.adapter;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.InvitationRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.Invitation;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.InvitationToken;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.InvitationJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.mapper.InvitationMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.mapper.UserMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository.JpaInvitationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class InvitationJpaAdapter implements InvitationRepository {

    private final JpaInvitationRepository jpaInvitationRepository;
    private final InvitationMapper mapper;

    @Override
    public Invitation save(Invitation invitation) {
        InvitationJpaEntity savedInvitationEntity = jpaInvitationRepository.save(mapper.toEntity(invitation));
        return mapper.toDomain(savedInvitationEntity);
    }

    @Override
    public Optional<Invitation> findInvitationByEmail(Email email) {
        return jpaInvitationRepository.findByEmail(email.asString())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Invitation> findInvitationByToken(InvitationToken invitationToken) {
        return jpaInvitationRepository.findByInvitationToken(invitationToken.asString())
                .map(mapper::toDomain);
    }
}
