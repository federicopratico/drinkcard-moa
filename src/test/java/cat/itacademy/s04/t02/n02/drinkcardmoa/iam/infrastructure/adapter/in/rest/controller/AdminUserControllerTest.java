package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.GetUserByIdQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.ListUsersQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.DeleteUserByIdUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.GetUserByIdUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.ListUsersUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.UserSummaryResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper.AdminUserMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.adapter.in.rest.dto.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private ListUsersUseCase listUsersUseCase;

    @Mock
    private GetUserByIdUseCase getUserByIdUseCase;

    @Mock
    private DeleteUserByIdUseCase deleteUserByIdUseCase;

    private AdminUserController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminUserController(
                listUsersUseCase,
                getUserByIdUseCase,
                deleteUserByIdUseCase,
                new AdminUserMapper()
        );
    }

    @Test
    void getUsers_WhenNoFilters_ReturnsPagedUserSummaryResponse() {
        UserSummaryResult user = new UserSummaryResult(
                "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1",
                "Volunteer User",
                "volunteer@userId.com",
                "VOLUNTEER",
                "ACTIVE"
        );

        when(listUsersUseCase.execute(new ListUsersQuery(null, null, null, 0, 20, "email,asc")))
                .thenReturn(new PageResult<>(List.of(user), 0, 20, 1, 1));

        ResponseEntity<PageResponse<UserSummaryResponse>> response = controller.getUsers(
                null,
                null,
                null,
                0,
                20,
                "email,asc"
        );

        ArgumentCaptor<ListUsersQuery> queryCaptor = ArgumentCaptor.forClass(ListUsersQuery.class);

        verify(listUsersUseCase).execute(queryCaptor.capture());

        PageResponse<UserSummaryResponse> body = response.getBody();
        UserSummaryResponse userResponse = body.content().getFirst();
        ListUsersQuery query = queryCaptor.getValue();

        assertAll(
                () -> assertEquals(200, response.getStatusCode().value()),
                () -> assertNotNull(body),
                () -> assertEquals(0, body.page()),
                () -> assertEquals(20, body.size()),
                () -> assertEquals(1, body.totalElements()),
                () -> assertEquals(1, body.totalPages()),
                () -> assertEquals(user.userId(), userResponse.userId()),
                () -> assertEquals(user.fullName(), userResponse.fullName()),
                () -> assertEquals(user.email(), userResponse.email()),
                () -> assertEquals(user.role(), userResponse.role()),
                () -> assertEquals(user.status(), userResponse.status()),
                () -> assertEquals(null, query.role()),
                () -> assertEquals(null, query.status()),
                () -> assertEquals(null, query.email()),
                () -> assertEquals(0, query.page()),
                () -> assertEquals(20, query.size()),
                () -> assertEquals("email,asc", query.sort())
        );
    }

    @Test
    void getUsers_WhenFiltersAreProvided_PassesQueryToUseCase() {
        when(listUsersUseCase.execute(new ListUsersQuery("ADMIN", "ACTIVE", "admin@userId.com", 1, 10, "lastName,desc")))
                .thenReturn(new PageResult<>(List.of(), 1, 10, 0, 0));

        controller.getUsers(
                "ADMIN",
                "ACTIVE",
                "admin@userId.com",
                1,
                10,
                "lastName,desc"
        );

        ArgumentCaptor<ListUsersQuery> queryCaptor = ArgumentCaptor.forClass(ListUsersQuery.class);

        verify(listUsersUseCase).execute(queryCaptor.capture());

        ListUsersQuery query = queryCaptor.getValue();

        assertAll(
                () -> assertEquals("ADMIN", query.role()),
                () -> assertEquals("ACTIVE", query.status()),
                () -> assertEquals("admin@userId.com", query.email()),
                () -> assertEquals(1, query.page()),
                () -> assertEquals(10, query.size()),
                () -> assertEquals("lastName,desc", query.sort())
        );
    }

    @Test
    void getUsers_ShouldRequireAdminRole() throws NoSuchMethodException {
        Method method = AdminUserController.class.getMethod(
                "getUsers",
                String.class,
                String.class,
                String.class,
                int.class,
                int.class,
                String.class
        );

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertAll(
                () -> assertNotNull(preAuthorize),
                () -> assertEquals("hasRole('ADMIN')", preAuthorize.value())
        );
    }

    @Test
    void getUserById_WhenUserExists_ReturnsUserSummary() {
        String userId = "8799df50-d517-4693-9e46-51b537c305a2";

        UserSummaryResult result = new UserSummaryResult(
                userId,
                "Admin User",
                "admin@userId.com",
                "ADMIN",
                "ACTIVE"
        );

        when(getUserByIdUseCase.execute(new GetUserByIdQuery(userId)))
                .thenReturn(result);

        ResponseEntity<UserSummaryResponse> response = controller.getUserById(userId);

        ArgumentCaptor<GetUserByIdQuery> queryCaptor = ArgumentCaptor.forClass(GetUserByIdQuery.class);

        verify(getUserByIdUseCase).execute(queryCaptor.capture());

        UserSummaryResponse body = response.getBody();

        assertAll(
                () -> assertEquals(200, response.getStatusCode().value()),
                () -> assertNotNull(body),
                () -> assertEquals(userId, body.userId()),
                () -> assertEquals("Admin User", body.fullName()),
                () -> assertEquals("admin@userId.com", body.email()),
                () -> assertEquals("ADMIN", body.role()),
                () -> assertEquals("ACTIVE", body.status()),
                () -> assertEquals(userId, queryCaptor.getValue().userId())
        );
    }
}
