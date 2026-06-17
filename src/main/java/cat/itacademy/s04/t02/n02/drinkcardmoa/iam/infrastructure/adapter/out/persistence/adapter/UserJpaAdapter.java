package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.adapter;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.query.UserSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.mapper.UserMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository.JpaUserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
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
public class UserJpaAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper mapper;

    @Override
    public User save(User user) {
        UserJpaEntity savedUserEntity = jpaUserRepository.save(mapper.toEntity(user));
        return mapper.toDomain(savedUserEntity);
    }

    @Override
    public void delete(User user) {
        jpaUserRepository.delete(mapper.toEntity(user));
    }

    @Override
    public Optional<User> findUserByEmail(Email email) {
        return jpaUserRepository.findByEmail(email.asString())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaUserRepository.existsByEmail(email.asString());
    }

    @Override
    public PageResult<User> searchUsers(UserSearchCriteria criteria) {
        Sort.Direction direction = Sort.Direction.fromString(criteria.sortDirection());
        PageRequest pageRequest = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by(direction, criteria.sortBy())
        );

        Page<UserJpaEntity> page = jpaUserRepository.findAll(toSpecification(criteria), pageRequest);

        List<User> users = page.getContent()
                .stream()
                .map(mapper::toDomain)
                .toList();

        return new PageResult<>(
                users,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public Optional<User> findById(VolunteerID volunteerID) {
        return jpaUserRepository.findById(volunteerID.asString())
                .map(mapper::toDomain);
    }

    private Specification<UserJpaEntity> toSpecification(UserSearchCriteria criteria) {
        Specification<UserJpaEntity> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (criteria.role() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("role"), criteria.role().name()));
        }

        if (criteria.status() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), criteria.status().name()));
        }

        if (criteria.email() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("email"), criteria.email().asString()));
        }

        return specification;
    }
}
