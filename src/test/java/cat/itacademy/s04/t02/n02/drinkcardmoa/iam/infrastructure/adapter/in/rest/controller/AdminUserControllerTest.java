package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.UserSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.ListUsersUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper.AdminUserMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getUsers_ReturnsUserSummaryList() throws Exception {
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

        when(listUsersUseCase.execute()).thenReturn(result);

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
    }

    @Test
    void getUsers_WhenNoUsers_ReturnsEmptyList() throws Exception {
        when(listUsersUseCase.execute()).thenReturn(List.of());

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("admin@email.com", null, "ROLE_ADMIN");

        mockMvc.perform(get("/api/v1/admin/users")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
