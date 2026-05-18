package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.command.CurrentUserCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.dto.result.CurrentUserResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.in.usecase.GetCurrentUserUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.mapper.UserMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(UserMapper.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetCurrentUserUseCase getCurrentUserUseCase;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getCurrentUser_ReturnsCurrentUserResponse() throws Exception {
        String userId = "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1";
        String email = "email@email.com";

        CurrentUserResult result = new CurrentUserResult(
                userId,
                "fullName",
                email,
                "VOLUNTEER",
                "ACTIVE"
        );

        when(getCurrentUserUseCase.execute(new CurrentUserCommand(userId)))
                .thenReturn(result);

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(userId, null, "ROLE_VOLUNTEER");

        mockMvc.perform(get("/api/v1/users/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.fullName").value("fullName"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("VOLUNTEER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        ArgumentCaptor<CurrentUserCommand> commandCaptor =
                ArgumentCaptor.forClass(CurrentUserCommand.class);

        verify(getCurrentUserUseCase).execute(commandCaptor.capture());

        assertEquals(userId, commandCaptor.getValue().userId());
    }
}
