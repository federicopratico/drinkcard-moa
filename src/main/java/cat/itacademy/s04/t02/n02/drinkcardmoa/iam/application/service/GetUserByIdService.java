package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.GetUserByIdQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.GetUserByIdUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.DrinkCardDirectory;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.UserNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserByIdService implements GetUserByIdUseCase {

    private final UserRepository userRepository;
    private final DrinkCardDirectory drinkCardDirectory;

    @Override
    public UserSummaryResult execute(GetUserByIdQuery query) {
        VolunteerID volunteerID = VolunteerID.from(query.userId());
        return userRepository.findById(volunteerID)
                .map(user -> {
                    var drinkCard = drinkCardDirectory.findByVolunteerId(volunteerID);
                    return UserSummaryResult.from(user, drinkCard);
                })
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with id: " + query.userId()));
    }
}
