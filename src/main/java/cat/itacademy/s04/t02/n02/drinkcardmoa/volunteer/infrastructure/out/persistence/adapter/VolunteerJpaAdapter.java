package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.out.persistence.adapter;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.application.port.out.VolunteerRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.domain.model.Volunteer;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.out.persistence.entity.VolunteerJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.out.persistence.mapper.VolunteerMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.out.persistence.repository.JpaVolunteerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class VolunteerJpaAdapter implements VolunteerRepository {

    private final JpaVolunteerRepository jpaVolunteerRepository;
    private final VolunteerMapper mapper;

    @Override
    public Volunteer save(Volunteer volunteer) {
        VolunteerJpaEntity entity = jpaVolunteerRepository.save(mapper.toEntity(volunteer));
        return mapper.toDomain(entity);

    }

    @Override
    public Optional<Volunteer> findByVolunteerId(VolunteerID volunteerID) {
        return jpaVolunteerRepository.findByVolunteerId(volunteerID.asString())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByVolunteerId(VolunteerID volunteerID) {
        return jpaVolunteerRepository.existsByVolunteerId(volunteerID.asString());
    }
}
