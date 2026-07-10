package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkCardAccountJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository.JpaDrinkCardAccountRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AdminPaymentEndpointTestIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("festival_test")
            .withUsername("postgres")
            .withPassword("postgres");

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
    private String volunteerId;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jpaPaymentRepository.deleteAll();
        jpaDrinkCardAccountRepository.deleteAll();
        jpaUserRepository.deleteAll();

        String adminId = VolunteerID.generate().asString();
        volunteerId = VolunteerID.generate().asString();

        jpaUserRepository.save(userEntity(
                adminId,
                "Admin",
                "User",
                "admin@add-card.test",
                "ADMIN",
                "ACTIVE"
        ));
        jpaUserRepository.save(userEntity(
                volunteerId,
                "Volunteer",
                "User",
                "volunteer@add-card.test",
                "VOLUNTEER",
                "ACTIVE"
        ));

        adminToken = tokenService.generateToken(user(adminId, "Admin", "User", "admin@add-card.test", Role.ADMIN, UserStatus.ACTIVE));
        volunteerToken = tokenService.generateToken(user(volunteerId, "Volunteer", "User", "volunteer@add-card.test", Role.VOLUNTEER, UserStatus.ACTIVE));
    }

    @Test
    void addDrinkCard_WhenAdminAndAccountCanPurchase_ReturnsResultAndPersistsPaymentAndCredits() throws Exception {
        seedAccount(volunteerId, 0, Instant.now().minus(2, ChronoUnit.DAYS), "ACTIVE");

        mockMvc.perform(post("/api/v1/admin/payments/add-drink-card")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addDrinkCardJson(volunteerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.volunteerId").value(volunteerId))
                .andExpect(jsonPath("$.credits").value(5))
                .andExpect(jsonPath("$.amount").value(10));

        DrinkCardAccountJpaEntity account = jpaDrinkCardAccountRepository.findByVolunteerId(volunteerId).orElseThrow();
        List<PaymentJpaEntity> payments = jpaPaymentRepository.findAll();

        assertEquals(1, payments.size());
        PaymentJpaEntity payment = payments.getFirst();

        assertAll(
                () -> assertEquals(5, account.getCredits()),
                () -> assertEquals(VolunteerID.from(volunteerId).value(), payment.getVolunteerId()),
                () -> assertEquals(PaymentStatus.SUCCESS.name(), payment.getStatus()),
                () -> assertEquals(0, Card.newCard().getPrice().compareTo(payment.getAmount())),
                () -> assertFalse("manual_payment".equals(payment.getIdempotencyKey())),
                () -> assertTrue(payment.getIdempotencyKey().startsWith(volunteerId)),
                () -> assertTrue(payment.getProviderCheckoutId() == null),
                () -> assertTrue(payment.getProviderCheckoutUrl() == null)
        );
    }

    @Test
    void addDrinkCard_WhenAuthenticatedUserIsNotAdmin_ReturnsForbidden() throws Exception {
        seedAccount(volunteerId, 0, Instant.now().minus(2, ChronoUnit.DAYS), "ACTIVE");

        mockMvc.perform(post("/api/v1/admin/payments/add-drink-card")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addDrinkCardJson(volunteerId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addDrinkCard_WhenNoBearerToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/admin/payments/add-drink-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addDrinkCardJson(volunteerId)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addDrinkCard_WhenAccountDoesNotExist_ReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/admin/payments/add-drink-card")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addDrinkCardJson(volunteerId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        assertEquals(0, jpaPaymentRepository.count());
    }

    @Test
    void addDrinkCard_WhenAccountAlreadyPurchasedToday_ReturnsBadRequest() throws Exception {
        seedAccount(volunteerId, 0, Instant.now(), "ACTIVE");

        mockMvc.perform(post("/api/v1/admin/payments/add-drink-card")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addDrinkCardJson(volunteerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertEquals(0, jpaPaymentRepository.count());
    }

    @Test
    void addDrinkCard_WhenRefillIsDisabled_ReturnsUnprocessableEntity() throws Exception {
        seedAccount(volunteerId, 0, Instant.now().minus(2, ChronoUnit.DAYS), "REFILL_DISABLED");

        mockMvc.perform(post("/api/v1/admin/payments/add-drink-card")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addDrinkCardJson(volunteerId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        assertEquals(0, jpaPaymentRepository.count());
    }

    private void seedAccount(String volunteerId, int credits, Instant lastPurchaseTimestamp, String status) {
        jpaDrinkCardAccountRepository.save(DrinkCardAccountJpaEntity.create(
                volunteerId,
                credits,
                lastPurchaseTimestamp,
                Instant.now().minus(5, ChronoUnit.DAYS),
                status
        ));
    }

    private String addDrinkCardJson(String volunteerId) {
        return "{\"volunteerId\":\"" + volunteerId + "\"}";
    }

    private UserJpaEntity userEntity(String id, String firstName, String lastName, String email, String role, String status) {
        return UserJpaEntity.create(
                id,
                firstName,
                lastName,
                email,
                "hashed_password",
                role,
                status
        );
    }

    private User user(String id, String firstName, String lastName, String email, Role role, UserStatus status) {
        return User.rehydrate(
                VolunteerID.from(id),
                FullName.from(firstName, lastName),
                Email.from(email),
                HashedPassword.from("hashed_password"),
                role,
                status
        );
    }
}
