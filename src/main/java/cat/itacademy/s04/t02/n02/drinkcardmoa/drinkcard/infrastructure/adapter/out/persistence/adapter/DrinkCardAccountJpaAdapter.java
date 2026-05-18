package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.adapter;

import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkCardAccountJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.mapper.DrinkCardAccountMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository.JpaDrinkCardAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class DrinkCardAccountJpaAdapter implements DrinkCardAccountRepository {

    private final JpaDrinkCardAccountRepository jpaDrinkCardAccountRepository;
    private final DrinkCardAccountMapper mapper;

    @Override
    public DrinkCardAccount save(DrinkCardAccount drinkCardAccount) {
        DrinkCardAccountJpaEntity entity = jpaDrinkCardAccountRepository.save(mapper.toEntity(drinkCardAccount));
        return mapper.toDomain(entity);

    }

    @Override
    public Optional<DrinkCardAccount> findByVolunteerId(VolunteerID volunteerID) {
        return jpaDrinkCardAccountRepository.findByVolunteerId(volunteerID.asString())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByVolunteerId(VolunteerID volunteerID) {
        return jpaDrinkCardAccountRepository.existsByVolunteerId(volunteerID.asString());
    }

    @Override
    public List<DrinkCardAccount> findAll() {
        return jpaDrinkCardAccountRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
