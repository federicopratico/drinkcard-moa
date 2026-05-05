package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.mapper;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserJpaEntity toEntity(User user) {
        return UserJpaEntity.create(
                user.getId().asString(),
                user.getFullName().firstName(),
                user.getFullName().lastName(),
                user.getEmail().asString(),
                user.getHashedPassword().value(),
                user.getRole().name()
        );
    }

    public User toDomain(UserJpaEntity entity) {
        return User.rehydrate(
                VolunteerID.from(entity.getId()),
                FullName.from(entity.getFirstName(), entity.getLastName()),
                Email.from(entity.getEmail()),
                HashedPassword.from(entity.getPassword()),
                Role.valueOf(entity.getRole())
        );
    }
}
