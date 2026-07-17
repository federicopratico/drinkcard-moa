package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.aggregate.DrinkTicket;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.DrinkType;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkCardAccountJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkTicketJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository.JpaDrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository.JpaDrinkTicketRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository.JpaPaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.TokenService;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Email;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.FullName;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.HashedPassword;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.Role;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.valueobject.UserStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository.JpaUserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AdminStatsEndpointTestIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("festival_test")
            .withUsername("postgres")
            .withPassword("postgres");
    @Autowired
    private JpaDrinkTicketRepository jpaDrinkTicketRepository;

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaDrinkCardAccountRepository jpaDrinkCardAccountRepository;

    @Autowired
    private JpaPaymentRepository jpaPaymentRepository;

    private String adminToken;
    private String volunteerToken;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jpaPaymentRepository.deleteAll();
        jpaDrinkCardAccountRepository.deleteAll();
        jpaUserRepository.deleteAll();

        String adminId = VolunteerID.generate().asString();
        String volunteerId = VolunteerID.generate().asString();

        jpaUserRepository.save(UserJpaEntity.create(adminId, "Admin", "User", "admin@stats.test", "hashed_password", "ADMIN", "ACTIVE"));
        jpaUserRepository.save(UserJpaEntity.create(volunteerId, "Volunteer", "User", "vol@stats.test", "hashed_password", "VOLUNTEER", "ACTIVE"));

        adminToken = tokenService.generateToken(user(adminId, "Admin", "User", "admin@stats.test", Role.ADMIN));
        volunteerToken = tokenService.generateToken(user(volunteerId, "Volunteer", "User", "vol@stats.test", Role.VOLUNTEER));
    }

    @Test
    void getStats_WhenAdmin_ReturnsAggregatedTotals() throws Exception {
        var vId1 = VolunteerID.generate();
        var vId2 = VolunteerID.generate();
        var vId3 = VolunteerID.generate();


        var v1 = vId1.asString();
        var v2 = vId2.asString();
        var v3 = vId3.asString();

        seedVolunteerUser(vId1, "v1@me.com");
        seedVolunteerUser(vId2, "v2@me.com");
        seedVolunteerUser(vId3, "v3@me.com");

        seedAccount(v1, 3, "ACTIVE");
        seedAccount(v2, 7, "ACTIVE");
        seedAccount(v3, 4, "REFILL_DISABLED");

        seedPayment(UUID.fromString(v1), new BigDecimal("10.00"), "SUCCESS");
        seedPayment(UUID.fromString(v2), new BigDecimal("15.50"), "SUCCESS");
        seedPayment(UUID.fromString(v1), new BigDecimal("99.99"), "PENDING");
        seedPayment(UUID.fromString(v2), new BigDecimal("42.00"), "FAILED");

        seedTicket(UUID.fromString(v1), "CONSUMED", DrinkType.BOA);
        seedTicket(UUID.fromString(v1), "CONSUMED", DrinkType.BOA);
        seedTicket(UUID.fromString(v2), "CONSUMED", DrinkType.PILS_BEER);

        seedTicket(UUID.fromString(v3), "PENDING", DrinkType.BOA);

        mockMvc.perform(get("/api/v1/admin/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAvailableCredits").value(14))
                .andExpect(jsonPath("$.totalSuccessfulPaymentsAmount").value(25.50))
                .andExpect(jsonPath("$.totalSuccessfulPayments").value(2))
                .andExpect(jsonPath("$.totalActiveCards").value(2))
                .andExpect(jsonPath("$.drinkConsumptions.size()").value(2))
                .andExpect(jsonPath("$.drinkConsumptions[0].drinkType").value("BOA"))
                .andExpect(jsonPath("$.drinkConsumptions[0].drinkTicketsCount").value(2))
                .andExpect(jsonPath("$.drinkConsumptions[1].drinkType").value("PILS_BEER"))
                .andExpect(jsonPath("$.drinkConsumptions[1].drinkTicketsCount").value(1))
                .andExpect(jsonPath("$.topVolunteers.size()").value(2))
                .andExpect(jsonPath("$.topVolunteers.[0].volunteer.email").value("v1@me.com"))
                .andExpect(jsonPath("$.topVolunteers.[0].drinkTicketsCount").value(2))
                .andExpect(jsonPath("$.topVolunteers.[1].volunteer.email").value("v2@me.com"))
                .andExpect(jsonPath("$.topVolunteers.[1].drinkTicketsCount").value(1));
    }

    @Test
    void getStats_WhenNoData_ReturnsZeros() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAvailableCredits").value(0))
                .andExpect(jsonPath("$.totalSuccessfulPaymentsAmount").value(0))
                .andExpect(jsonPath("$.topVolunteers.size()").value(0))
                .andExpect(jsonPath("$.drinkConsumptions.size()").value(0))
                .andExpect(jsonPath("$.totalActiveCards").value(0));
    }

    @Test
    void getStats_WhenAuthenticatedUserIsNotAdmin_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStats_WhenNoBearerToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    private void seedAccount(String volunteerId, int credits, String status) {
        jpaDrinkCardAccountRepository.save(DrinkCardAccountJpaEntity.create(
                volunteerId,
                credits,
                Instant.now().minus(2, ChronoUnit.DAYS),
                Instant.now().minus(5, ChronoUnit.DAYS),
                status
        ));
    }

    private void seedVolunteerUser(VolunteerID volunteerId, String email) {
        jpaUserRepository.save(UserJpaEntity.create(
                volunteerId.asString(),
                "Volunteer",
                "Test",
                email,
                "hashed_password",
                "VOLUNTEER",
                "ACTIVE"
        ));
    }

    private void seedTicket(UUID volunteerId, String status, DrinkType drinkType) {
        var ticket = DrinkTicketJpaEntity.create(
                UUID.randomUUID(),
                volunteerId,
                drinkType.name(),
                status,
                Instant.now().minus(2, ChronoUnit.DAYS),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS),
                null
        );
        jpaDrinkTicketRepository.save(ticket);
    }

    private void seedPayment(UUID volunteerId, BigDecimal amount, String status) {
        Instant now = Instant.now();
        jpaPaymentRepository.save(PaymentJpaEntity.create(
                UUID.randomUUID(),
                volunteerId,
                UUID.randomUUID().toString(),
                amount,
                status,
                null,
                null,
                "SUCCESS".equals(status) ? now : null,
                now,
                now.plus(1, ChronoUnit.HOURS),
                null
        ));
    }

    private User user(String id, String firstName, String lastName, String email, Role role) {
        return User.rehydrate(
                VolunteerID.from(id),
                FullName.from(firstName, lastName),
                Email.from(email),
                HashedPassword.from("hashed_password"),
                role,
                UserStatus.ACTIVE
        );
    }
}
