package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.controller;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.GetDrinkCardAccountByVolunteerIdQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.query.ListDrinkCardAccountsQuery;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.DrinkCardAccountSummaryResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.GetDrinkCardAccountByVolunteerIdUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.usecase.ListDrinkCardAccountsUseCase;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.in.rest.mapper.AdminDrinkCardAccountMapper;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.application.dto.PageResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure.GlobalExceptionHandler;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminDrinkCardAccountController.class)
@Import({AdminDrinkCardAccountMapper.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class AdminDrinkCardAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListDrinkCardAccountsUseCase listDrinkCardAccountsUseCase;

    @MockitoBean
    private GetDrinkCardAccountByVolunteerIdUseCase getDrinkCardAccountByVolunteerIdUseCase;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getDrinkCardAccounts_WhenAccountsExist_ReturnsPagedDrinkCardAccountResponse() throws Exception {
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

        ListDrinkCardAccountsQuery query = new ListDrinkCardAccountsQuery(
                "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1",
                "ACTIVE",
                1,
                10,
                "credits,desc"
        );

        when(listDrinkCardAccountsUseCase.execute(query))
                .thenReturn(new PageResult<>(result, 1, 10, 35, 4));

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "admin-user-id",
                        null,
                        "ROLE_ADMIN"
                );

        mockMvc.perform(get("/api/v1/admin/drink-card-accounts")
                        .param("volunteerId", "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1")
                        .param("status", "ACTIVE")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "credits,desc")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(35))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.content[0].volunteerId").value("4f0a8db1-63a7-4997-944c-9f2f6b82e6d1"))
                .andExpect(jsonPath("$.content[0].credits").value(4))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].lastPurchaseTimestamp").value("2026-05-18T10:00:00Z"))
                .andExpect(jsonPath("$.content[1].volunteerId").value("8799df50-d517-4693-9e46-51b537c305a2"))
                .andExpect(jsonPath("$.content[1].credits").value(0))
                .andExpect(jsonPath("$.content[1].status").value("SUSPENDED"))
                .andExpect(jsonPath("$.content[1].lastPurchaseTimestamp").doesNotExist());

        verify(listDrinkCardAccountsUseCase).execute(query);
    }

    @Test
    void getDrinkCardAccounts_WhenNoAccountsExist_ReturnsEmptyPage() throws Exception {
        ListDrinkCardAccountsQuery query = new ListDrinkCardAccountsQuery(
                null,
                null,
                0,
                20,
                "volunteerId,asc"
        );

        when(listDrinkCardAccountsUseCase.execute(query))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "admin-user-id",
                        null,
                        "ROLE_ADMIN"
                );

        mockMvc.perform(get("/api/v1/admin/drink-card-accounts")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(listDrinkCardAccountsUseCase).execute(query);
    }

    @Test
    void getDrinkCardAccountByVolunteerId_WhenAccountExists_ReturnsDrinkCardAccount() throws Exception {
        String volunteerId = "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1";
        Instant lastPurchaseTimestamp = Instant.parse("2026-05-18T10:00:00Z");
        DrinkCardAccountSummaryResult result = new DrinkCardAccountSummaryResult(
                volunteerId,
                4,
                "ACTIVE",
                lastPurchaseTimestamp
        );

        when(getDrinkCardAccountByVolunteerIdUseCase.execute(new GetDrinkCardAccountByVolunteerIdQuery(volunteerId)))
                .thenReturn(result);

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "admin-user-id",
                        null,
                        "ROLE_ADMIN"
                );

        mockMvc.perform(get("/api/v1/admin/drink-card-accounts/{volunteerId}", volunteerId)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.volunteerId").value(volunteerId))
                .andExpect(jsonPath("$.credits").value(4))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.lastPurchaseTimestamp").value("2026-05-18T10:00:00Z"));

        verify(getDrinkCardAccountByVolunteerIdUseCase).execute(new GetDrinkCardAccountByVolunteerIdQuery(volunteerId));
    }

    @Test
    void getDrinkCardAccountByVolunteerId_WhenAccountDoesNotExist_ReturnsNotFound() throws Exception {
        String volunteerId = "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1";

        when(getDrinkCardAccountByVolunteerIdUseCase.execute(any(GetDrinkCardAccountByVolunteerIdQuery.class)))
                .thenThrow(new DrinkCardAccountNotFoundException("DrinkCardAccount not found with id: " + volunteerId));

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "admin-user-id",
                        null,
                        "ROLE_ADMIN"
                );

        mockMvc.perform(get("/api/v1/admin/drink-card-accounts/{volunteerId}", volunteerId)
                        .principal(authentication))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("DrinkCardAccount not found with id: " + volunteerId));

        verify(getDrinkCardAccountByVolunteerIdUseCase).execute(new GetDrinkCardAccountByVolunteerIdQuery(volunteerId));
    }

    @Test
    void getDrinkCardAccounts_ShouldRequireAdminRole() throws NoSuchMethodException {
        Method method = AdminDrinkCardAccountController.class.getMethod(
                "getDrinkCardAccounts",
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

}
