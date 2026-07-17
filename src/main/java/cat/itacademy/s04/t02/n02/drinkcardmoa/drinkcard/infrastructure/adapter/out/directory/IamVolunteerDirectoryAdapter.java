package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.directory;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.VolunteerDirectory;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.dto.VolunteerProfile;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class IamVolunteerDirectoryAdapter implements VolunteerDirectory {

    private final UserRepository userRepository;

    @Override
    public Map<VolunteerID, VolunteerProfile> findAllByIds(Collection<VolunteerID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(User::getId, toProfile()));
    }

    private Function<User, VolunteerProfile> toProfile() {
        return user -> new VolunteerProfile(
                user.getId(),
                user.getFullName().firstName(),
                user.getFullName().lastName(),
                user.getEmail().asString()
        );
    }
}
