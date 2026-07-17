package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.directory;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.DrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.out.dto.VolunteerProfile;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.DrinkCardDirectory;
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
public class DrinkCardDirectoryAdapter implements DrinkCardDirectory {

    private final DrinkCardAccountRepository drinkCardAccountRepository;

    @Override
    public Map<String, DrinkCardAccountSummaryResult> findAllByVolunteerIds(Collection<VolunteerID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        return drinkCardAccountRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(account -> account.getVolunteerId().asString(), DrinkCardAccountSummaryResult::from));

    }

    @Override
    public DrinkCardAccountSummaryResult findByVolunteerId(VolunteerID id) {
        return drinkCardAccountRepository.findByVolunteerId(id)
                .map(DrinkCardAccountSummaryResult::from)
                .orElse(null);
    }
}
