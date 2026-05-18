package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetCurrentDrinkCardAccountQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CurrentDrinkCardAccountResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.GetCurrentDrinkCardAccountUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper.DrinkCardAccountControllerMapper;
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

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DrinkCardAccountController.class)
@Import(DrinkCardAccountControllerMapper.class)
@AutoConfigureMockMvc(addFilters = false)
class DrinkCardAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetCurrentDrinkCardAccountUseCase getCurrentDrinkCardAccountUseCase;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getCurrentDrinkCardAccount_ReturnsAuthenticatedUsersDrinkCardAccount() throws Exception {
        String volunteerId = "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1";
        Instant lastPurchaseTimestamp = Instant.parse("2026-05-18T10:00:00Z");
        CurrentDrinkCardAccountResult result = new CurrentDrinkCardAccountResult(
                volunteerId,
                4,
                "ACTIVE",
                lastPurchaseTimestamp
        );

        when(getCurrentDrinkCardAccountUseCase.execute(new GetCurrentDrinkCardAccountQuery(volunteerId)))
                .thenReturn(result);

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(volunteerId, null, "ROLE_VOLUNTEER");

        mockMvc.perform(get("/api/v1/drink-card-accounts/me")
                        .param("volunteerId", "ignored-client-id")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.volunteerId").value(volunteerId))
                .andExpect(jsonPath("$.credits").value(4))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.lastPurchaseTimestamp").value("2026-05-18T10:00:00Z"));

        ArgumentCaptor<GetCurrentDrinkCardAccountQuery> queryCaptor =
                ArgumentCaptor.forClass(GetCurrentDrinkCardAccountQuery.class);

        verify(getCurrentDrinkCardAccountUseCase).execute(queryCaptor.capture());
        assertEquals(volunteerId, queryCaptor.getValue().userId());
    }
}
