package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.ListUsersQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.*;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private ListUsersService listUsersService;

    @Test
    void execute_WhenNoFilters_ReturnUserSummaryList() {
        User user = createUser("volunteer@userId.com", "Volunteer", "User", Role.VOLUNTEER, UserStatus.ACTIVE);

        when(userRepository.findAllByFilters(null, null, null))
                .thenReturn(List.of(user));

        List<UserSummaryResult> result = listUsersService.execute(
                new ListUsersQuery(null, null, null)
        );

        assertEquals(1, result.size());
        assertEquals("Volunteer User", result.getFirst().fullName());
        assertEquals("volunteer@userid.com", result.getFirst().email());
        assertEquals("VOLUNTEER", result.getFirst().role());
        assertEquals("ACTIVE", result.getFirst().status());

        verify(userRepository).findAllByFilters(null, null, null);
    }

    @Test
    void execute_WhenFiltersExist_PassParsedFiltersToRepository() {
        User user = createUser("admin@userId.com", "Admin", "User", Role.ADMIN, UserStatus.SUSPENDED);

        when(userRepository.findAllByFilters(any(Role.class), any(UserStatus.class), any(Email.class)))
                .thenReturn(List.of(user));

        List<UserSummaryResult> result = listUsersService.execute(
                new ListUsersQuery("ADMIN", "SUSPENDED", "admin@userId.com")
        );

        assertEquals(1, result.size());
        assertEquals("Admin User", result.getFirst().fullName());

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        ArgumentCaptor<UserStatus> statusCaptor = ArgumentCaptor.forClass(UserStatus.class);
        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);

        verify(userRepository).findAllByFilters(
                roleCaptor.capture(),
                statusCaptor.capture(),
                emailCaptor.capture()
        );

        assertEquals(Role.ADMIN, roleCaptor.getValue());
        assertEquals(UserStatus.SUSPENDED, statusCaptor.getValue());
        assertEquals("admin@userid.com", emailCaptor.getValue().asString());
    }

    @Test
    void execute_WhenNoUsersMatch_ReturnEmptyList() {
        when(userRepository.findAllByFilters(null, UserStatus.DELETED, null))
                .thenReturn(List.of());

        List<UserSummaryResult> result = listUsersService.execute(
                new ListUsersQuery(null, "DELETED", null)
        );

        assertTrue(result.isEmpty());

        verify(userRepository).findAllByFilters(null, UserStatus.DELETED, null);
    }

    private User createUser(String email, String firstName, String lastName, Role role, UserStatus status) {
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
