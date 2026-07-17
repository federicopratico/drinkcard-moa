package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkCardAccount;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkCardAccountStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.GetUserByIdQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.DrinkCardDirectory;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.UserNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserByIdServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DrinkCardDirectory drinkCardDirectory;

    @InjectMocks
    private GetUserByIdService getUserByIdService;

    @Test
    void execute_WhenUserExists_ReturnUserSummary() {
        VolunteerID userId = VolunteerID.generate();
        User user = createUser(userId);

        when(userRepository.findById(any(VolunteerID.class)))
                .thenReturn(Optional.of(user));

        when(drinkCardDirectory.findByVolunteerId(any(VolunteerID.class)))
                .thenReturn(DrinkCardAccountSummaryResult.from(DrinkCardAccount.create(userId)));

        UserSummaryResult result = getUserByIdService.execute(
                new GetUserByIdQuery(userId.asString())
        );

        assertNotNull(result);
        assertEquals(userId.asString(), result.userId());
        assertEquals("First Last", result.fullName());
        assertEquals("user@userid.com", result.email());
        assertEquals("VOLUNTEER", result.role());
        assertEquals("ACTIVE", result.status());
        assertEquals(0, result.drinkCard().credits());

        ArgumentCaptor<VolunteerID> userIdCaptor =
                ArgumentCaptor.forClass(VolunteerID.class);

        verify(userRepository).findById(userIdCaptor.capture());

        assertEquals(userId.asString(), userIdCaptor.getValue().asString());
    }

    @Test
    void execute_WhenUserDoesNotExist_ThrowUserNotFoundException() {
        VolunteerID userId = VolunteerID.generate();

        when(userRepository.findById(any(VolunteerID.class)))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            getUserByIdService.execute(new GetUserByIdQuery(userId.asString()));
        });

        verify(userRepository, times(1)).findById(any(VolunteerID.class));
    }

    private User createUser(VolunteerID userId) {
        return User.rehydrate(
                userId,
                FullName.from("First", "Last"),
                Email.from("user@userId.com"),
                HashedPassword.from("hashed_password"),
                Role.VOLUNTEER,
                UserStatus.ACTIVE
        );
    }
}
