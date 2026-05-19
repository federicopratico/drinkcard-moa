package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.ListUsersQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.UserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.query.UserSearchCriteria;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ListUsersService listUsersService;

    @Test
    void execute_WhenNoFilters_ReturnPagedUserSummaryListWithDefaults() {
        User user = createUser("volunteer@userId.com", "Volunteer", "User", Role.VOLUNTEER, UserStatus.ACTIVE);

        when(userRepository.searchUsers(any(UserSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(user), 0, 20, 1, 1));

        PageResult<UserSummaryResult> result = listUsersService.execute(
                new ListUsersQuery(null, null, null, -1, 0, null)
        );

        ArgumentCaptor<UserSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(UserSearchCriteria.class);

        verify(userRepository).searchUsers(criteriaCaptor.capture());

        UserSearchCriteria criteria = criteriaCaptor.getValue();
        UserSummaryResult userResult = result.content().getFirst();

        assertAll(
                () -> assertNull(criteria.role()),
                () -> assertNull(criteria.status()),
                () -> assertNull(criteria.email()),
                () -> assertEquals(0, criteria.page()),
                () -> assertEquals(20, criteria.size()),
                () -> assertEquals("email", criteria.sortBy()),
                () -> assertEquals("asc", criteria.sortDirection()),
                () -> assertEquals(1, result.content().size()),
                () -> assertEquals("Volunteer User", userResult.fullName()),
                () -> assertEquals("volunteer@userid.com", userResult.email()),
                () -> assertEquals("VOLUNTEER", userResult.role()),
                () -> assertEquals("ACTIVE", userResult.status())
        );
    }

    @Test
    void execute_WhenFiltersExist_PassParsedFiltersToRepository() {
        User user = createUser("admin@userId.com", "Admin", "User", Role.ADMIN, UserStatus.SUSPENDED);

        when(userRepository.searchUsers(any(UserSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(user), 2, 10, 1, 1));

        PageResult<UserSummaryResult> result = listUsersService.execute(
                new ListUsersQuery("ADMIN", "SUSPENDED", "admin@userId.com", 2, 10, "lastName,desc")
        );

        assertEquals(1, result.content().size());
        assertEquals("Admin User", result.content().getFirst().fullName());

        ArgumentCaptor<UserSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(UserSearchCriteria.class);

        verify(userRepository).searchUsers(criteriaCaptor.capture());

        UserSearchCriteria criteria = criteriaCaptor.getValue();

        assertAll(
                () -> assertEquals(Role.ADMIN, criteria.role()),
                () -> assertEquals(UserStatus.SUSPENDED, criteria.status()),
                () -> assertEquals("admin@userid.com", criteria.email().asString()),
                () -> assertEquals(2, criteria.page()),
                () -> assertEquals(10, criteria.size()),
                () -> assertEquals("lastName", criteria.sortBy()),
                () -> assertEquals("desc", criteria.sortDirection())
        );
    }

    @Test
    void execute_WhenNoUsersMatch_ReturnEmptyPage() {
        when(userRepository.searchUsers(any(UserSearchCriteria.class)))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));

        PageResult<UserSummaryResult> result = listUsersService.execute(
                new ListUsersQuery(null, "DELETED", null, 0, 20, null)
        );

        assertTrue(result.content().isEmpty());

        verify(userRepository).searchUsers(any(UserSearchCriteria.class));
    }

    @Test
    void execute_WhenSortFieldIsInvalid_ShouldThrowIllegalArgumentException() {
        ListUsersQuery query = new ListUsersQuery(null, null, null, 0, 20, "createdAt,desc");

        assertThrows(
                IllegalArgumentException.class,
                () -> listUsersService.execute(query)
        );
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
