package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.CurrentUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.CurrentUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.UserNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetCurrentUserService getCurrentUserService;

    @Test
    void execute_WhenUserExists_ReturnCurrentUser() {
        User user = createUser();
        CurrentUserCommand command = new CurrentUserCommand(user.getId().asString());

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        CurrentUserResult result = getCurrentUserService.execute(command);

        assertNotNull(result);
        assertEquals(user.getId().asString(), result.userId());
        assertEquals(user.getFullName().asString(), result.fullName());
        assertEquals(user.getEmail().value(), result.email());
        assertEquals(user.getRole().name(), result.role());

        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    void execute_WhenUserDoesNotExist_ThrowUserNotFoundException() {
        VolunteerID missingUserId = VolunteerID.generate();
        CurrentUserCommand command = new CurrentUserCommand(missingUserId.asString());

        when(userRepository.findById(missingUserId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            getCurrentUserService.execute(command);
        });

        verify(userRepository, times(1)).findById(missingUserId);
    }

    private User createUser() {
        return User.rehydrate(
                VolunteerID.generate(),
                FullName.from("firstName", "lastName"),
                Email.from("userId@userId.com"),
                HashedPassword.from("hashed_password"),
                Role.VOLUNTEER,
                UserStatus.ACTIVE
        );
    }
}
