package cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.out.persistence.repository;

import cat.itacademy.s04.t02.n02.drinkcardmoa.volunteer.infrastructure.adapter.out.persistence.entity.VolunteerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaVolunteerRepository extends JpaRepository <VolunteerJpaEntity, String> {
    Optional<VolunteerJpaEntity> findByVolunteerId(String volunteerID);
    boolean existsByVolunteerId(String volunteerID);
}
