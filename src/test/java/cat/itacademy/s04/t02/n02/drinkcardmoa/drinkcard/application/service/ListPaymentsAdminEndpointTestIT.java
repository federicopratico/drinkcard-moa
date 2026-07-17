package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
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

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ListPaymentsAdminEndpointTestIT {

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
    private JpaPaymentRepository jpaPaymentRepository;

    private String adminToken;
    private String knownVolunteerId;
    private String orphanVolunteerId;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jpaPaymentRepository.deleteAll();
        jpaUserRepository.deleteAll();

        String adminId = VolunteerID.generate().asString();
        knownVolunteerId = VolunteerID.generate().asString();
        orphanVolunteerId = VolunteerID.generate().asString();

        jpaUserRepository.save(userEntity(adminId, "Admin", "User", "admin@list-payments.test", "ADMIN"));
        jpaUserRepository.save(userEntity(knownVolunteerId, "Ada", "Lovelace", "ada@list-payments.test", "VOLUNTEER"));

        seedPayment(knownVolunteerId, "PENDING", Instant.now().minus(2, ChronoUnit.HOURS));
        seedPayment(orphanVolunteerId, "PENDING", Instant.now().minus(1, ChronoUnit.HOURS));

        adminToken = tokenService.generateToken(user(adminId, "Admin", "User", "admin@list-payments.test", Role.ADMIN));
    }

    @Test
    void listPayments_WhenAdmin_ReturnsPaymentsEnrichedWithVolunteerInfo() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                // Sorted by createdAt desc — orphan is first (newer)
                .andExpect(jsonPath("$.content[0].volunteer.id").value(orphanVolunteerId))
                .andExpect(jsonPath("$.content[0].volunteer.firstName").value("unknown"))
                .andExpect(jsonPath("$.content[0].volunteer.lastName").value("unknown"))
                .andExpect(jsonPath("$.content[0].volunteer.email").value("unknown"))
                .andExpect(jsonPath("$.content[1].volunteer.id").value(knownVolunteerId))
                .andExpect(jsonPath("$.content[1].volunteer.firstName").value("Ada"))
                .andExpect(jsonPath("$.content[1].volunteer.lastName").value("Lovelace"))
                .andExpect(jsonPath("$.content[1].volunteer.email").value("ada@list-payments.test"))
                .andExpect(jsonPath("$.content[1].volunteerId").value(knownVolunteerId))
                .andExpect(jsonPath("$.content[0].status", anyOf(is("PENDING"), is(PaymentStatus.PENDING.name()))));
    }

    @Test
    void listPayments_WhenNoBearerToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payments"))
                .andExpect(status().isUnauthorized());
    }

    private void seedPayment(String volunteerId, String status, Instant createdAt) {
        UUID paymentId = UUID.randomUUID();
        jpaPaymentRepository.save(PaymentJpaEntity.create(
                paymentId,
                UUID.fromString(volunteerId),
                paymentId.toString(),
                new BigDecimal("10.00"),
                status,
                null,
                null,
                null,
                createdAt,
                createdAt.plus(30, ChronoUnit.MINUTES),
                null
        ));
    }

    private UserJpaEntity userEntity(String id, String firstName, String lastName, String email, String role) {
        return UserJpaEntity.create(
                id,
                firstName,
                lastName,
                email,
                "hashed_password",
                role,
                "ACTIVE"
        );
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
