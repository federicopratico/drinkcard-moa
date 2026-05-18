package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.ListUsersQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.query.GetUserByIdQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.GetUserByIdUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.ListUsersUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper.AdminUserMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import(AdminUserMapper.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListUsersUseCase listUsersUseCase;

    @MockitoBean
    private GetUserByIdUseCase getUserByIdUseCase;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getUsers_WhenNoFilters_ReturnsUserSummaryList() throws Exception {
        List<UserSummaryResult> result = List.of(
                new UserSummaryResult(
                        "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1",
                        "Volunteer User",
                        "volunteer@email.com",
                        "VOLUNTEER",
                        "ACTIVE"
                ),
                new UserSummaryResult(
                        "8799df50-d517-4693-9e46-51b537c305a2",
                        "Admin User",
                        "admin@email.com",
                        "ADMIN",
                        "SUSPENDED"
                )
        );

        when(listUsersUseCase.execute(new ListUsersQuery(null, null, null)))
                .thenReturn(result);

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("admin@email.com", null, "ROLE_ADMIN");

        mockMvc.perform(get("/api/v1/admin/users")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("4f0a8db1-63a7-4997-944c-9f2f6b82e6d1"))
                .andExpect(jsonPath("$[0].fullName").value("Volunteer User"))
                .andExpect(jsonPath("$[0].email").value("volunteer@email.com"))
                .andExpect(jsonPath("$[0].role").value("VOLUNTEER"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].userId").value("8799df50-d517-4693-9e46-51b537c305a2"))
                .andExpect(jsonPath("$[1].fullName").value("Admin User"))
                .andExpect(jsonPath("$[1].email").value("admin@email.com"))
                .andExpect(jsonPath("$[1].role").value("ADMIN"))
                .andExpect(jsonPath("$[1].status").value("SUSPENDED"));

        ArgumentCaptor<ListUsersQuery> queryCaptor =
                ArgumentCaptor.forClass(ListUsersQuery.class);

        verify(listUsersUseCase).execute(queryCaptor.capture());

        assertEquals(null, queryCaptor.getValue().role());
        assertEquals(null, queryCaptor.getValue().status());
        assertEquals(null, queryCaptor.getValue().email());
    }

    @Test
    void getUsers_WhenFiltersAreProvided_PassesQueryToUseCase() throws Exception {
        List<UserSummaryResult> result = List.of(
                new UserSummaryResult(
                        "8799df50-d517-4693-9e46-51b537c305a2",
                        "Admin User",
                        "admin@email.com",
                        "ADMIN",
                        "ACTIVE"
                )
        );

        when(listUsersUseCase.execute(new ListUsersQuery("ADMIN", "ACTIVE", "admin@email.com")))
                .thenReturn(result);

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("admin@email.com", null, "ROLE_ADMIN");

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("role", "ADMIN")
                        .param("status", "ACTIVE")
                        .param("email", "admin@email.com")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("8799df50-d517-4693-9e46-51b537c305a2"))
                .andExpect(jsonPath("$[0].fullName").value("Admin User"))
                .andExpect(jsonPath("$[0].email").value("admin@email.com"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        ArgumentCaptor<ListUsersQuery> queryCaptor =
                ArgumentCaptor.forClass(ListUsersQuery.class);

        verify(listUsersUseCase).execute(queryCaptor.capture());

        assertEquals("ADMIN", queryCaptor.getValue().role());
        assertEquals("ACTIVE", queryCaptor.getValue().status());
        assertEquals("admin@email.com", queryCaptor.getValue().email());
    }

    @Test
    void getUsers_WhenNoUsersMatch_ReturnsEmptyList() throws Exception {
        when(listUsersUseCase.execute(new ListUsersQuery("VOLUNTEER", "DELETED", null)))
                .thenReturn(List.of());

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("admin@email.com", null, "ROLE_ADMIN");

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("role", "VOLUNTEER")
                        .param("status", "DELETED")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getUserById_WhenUserExists_ReturnsUserSummary() throws Exception {
        String userId = "8799df50-d517-4693-9e46-51b537c305a2";

        UserSummaryResult result = new UserSummaryResult(
                userId,
                "Admin User",
                "admin@email.com",
                "ADMIN",
                "ACTIVE"
        );

        when(getUserByIdUseCase.execute(new GetUserByIdQuery(userId)))
                .thenReturn(result);

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("admin@email.com", null, "ROLE_ADMIN");

        mockMvc.perform(get("/api/v1/admin/users/{userId}", userId)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.fullName").value("Admin User"))
                .andExpect(jsonPath("$.email").value("admin@email.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        ArgumentCaptor<GetUserByIdQuery> queryCaptor =
                ArgumentCaptor.forClass(GetUserByIdQuery.class);

        verify(getUserByIdUseCase).execute(queryCaptor.capture());

        assertEquals(userId, queryCaptor.getValue().userId());
    }
}
