package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ListUsersService listUserService;

    @Test
    void execute_WhenUsersExist_ReturnUserSummaryList() {
        User volunteer = createUser(
                "volunteer@email.com",
                "Volunteer",
                "User",
                Role.VOLUNTEER,
                UserStatus.ACTIVE
        );

        User admin = createUser(
                "admin@email.com",
                "Admin",
                "User",
                Role.ADMIN,
                UserStatus.SUSPENDED
        );

        when(userRepository.findAll())
                .thenReturn(List.of(volunteer, admin));

        List<UserSummaryResult> result = listUserService.execute();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(volunteer.getId().asString(), result.getFirst().userId());
        assertEquals("Volunteer User", result.getFirst().fullName());
        assertEquals("volunteer@email.com", result.getFirst().email());
        assertEquals("VOLUNTEER", result.get(0).role());
        assertEquals("ACTIVE", result.get(0).status());

        assertEquals(admin.getId().asString(), result.get(1).userId());
        assertEquals("Admin User", result.get(1).fullName());
        assertEquals("admin@email.com", result.get(1).email());
        assertEquals("ADMIN", result.get(1).role());
        assertEquals("SUSPENDED", result.get(1).status());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void execute_WhenNoUsersExist_ReturnEmptyList() {
        when(userRepository.findAll())
                .thenReturn(List.of());

        List<UserSummaryResult> result = listUserService.execute();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findAll();
    }

    private User createUser(
            String email,
            String firstName,
            String lastName,
            Role role,
            UserStatus status
    ) {
        return User.rehydrate(
                VolunteerID.generate(),
                FullName.from(firstName, lastName),
                Email.from(email),
                HashedPassword.from("hashed_password"),
                role,
                status
        );
    }
}
