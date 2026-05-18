package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListDrinkCardAccountsUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper.AdminDrinkCardAccountMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminDrinkCardAccountController.class)
@Import(AdminDrinkCardAccountMapper.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminDrinkCardAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListDrinkCardAccountsUseCase listDrinkCardAccountsUseCase;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getDrinkCardAccounts_WhenAccountsExist_ReturnsDrinkCardAccountList() throws Exception {
        Instant lastPurchaseTimestamp = Instant.parse("2026-05-18T10:00:00Z");

        List<DrinkCardAccountSummaryResult> result = List.of(
                new DrinkCardAccountSummaryResult(
                        "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1",
                        4,
                        "ACTIVE",
                        lastPurchaseTimestamp
                ),
                new DrinkCardAccountSummaryResult(
                        "8799df50-d517-4693-9e46-51b537c305a2",
                        0,
                        "SUSPENDED",
                        null
                )
        );

        when(listDrinkCardAccountsUseCase.execute()).thenReturn(result);

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "admin-user-id",
                        null,
                        "ROLE_ADMIN"
                );

        mockMvc.perform(get("/api/v1/admin/drink-card-accounts")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].volunteerId").value("4f0a8db1-63a7-4997-944c-9f2f6b82e6d1"))
                .andExpect(jsonPath("$[0].credits").value(4))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].lastPurchaseTimestamp").value("2026-05-18T10:00:00Z"))
                .andExpect(jsonPath("$[1].volunteerId").value("8799df50-d517-4693-9e46-51b537c305a2"))
                .andExpect(jsonPath("$[1].credits").value(0))
                .andExpect(jsonPath("$[1].status").value("SUSPENDED"))
                .andExpect(jsonPath("$[1].lastPurchaseTimestamp").doesNotExist());

        verify(listDrinkCardAccountsUseCase).execute();
    }

    @Test
    void getDrinkCardAccounts_WhenNoAccountsExist_ReturnsEmptyList() throws Exception {
        when(listDrinkCardAccountsUseCase.execute()).thenReturn(List.of());

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "admin-user-id",
                        null,
                        "ROLE_ADMIN"
                );

        mockMvc.perform(get("/api/v1/admin/drink-card-accounts")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(listDrinkCardAccountsUseCase).execute();
    }
}
