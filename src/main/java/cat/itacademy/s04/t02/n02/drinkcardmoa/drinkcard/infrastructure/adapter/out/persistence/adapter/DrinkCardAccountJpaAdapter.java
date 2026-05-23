package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.adapter;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.query.DrinkCardAccountSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkCardAccountJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.mapper.DrinkCardAccountMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository.JpaDrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.out.persistence.JpaSpecificationBuilder;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    public PageResult<DrinkCardAccount> searchDrinkCardAccounts(DrinkCardAccountSearchCriteria criteria) {
        Sort.Direction direction = Sort.Direction.fromString(criteria.sortDirection());

        PageRequest pageRequest = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by(direction, criteria.sortBy())
        );

        Page<DrinkCardAccountJpaEntity> page = jpaDrinkCardAccountRepository.findAll(
                toSpecification(criteria),
                pageRequest
        );

        List<DrinkCardAccount> accounts = page.getContent()
                .stream()
                .map(mapper::toDomain)
                .toList();

        return new PageResult<>(
                accounts,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private Specification<DrinkCardAccountJpaEntity> toSpecification(DrinkCardAccountSearchCriteria criteria) {
        return JpaSpecificationBuilder.<DrinkCardAccountJpaEntity>builder()
                .equal("volunteerId", criteria.volunteerId() == null ? null : criteria.volunteerId().asString())
                .equal("status", criteria.status() == null ? null : criteria.status().name())
                .build();
    }
}
