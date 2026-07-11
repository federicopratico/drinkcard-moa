package cat.itacademy.s04.t02.n02.drinkcardmoa.turn.application.service;

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
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.out.persistence.entity.TurnJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.turn.infrastructure.adapter.out.persistence.repository.JpaTurnRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AdminTurnEndpointTestIT {

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
    private JpaTurnRepository jpaTurnRepository;

    private String adminToken;
    private String volunteerToken;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jpaTurnRepository.deleteAll();
        jpaUserRepository.deleteAll();

        String adminId = VolunteerID.generate().asString();
        String volunteerId = VolunteerID.generate().asString();

        jpaUserRepository.save(userEntity(adminId, "Admin", "User", "admin@add-turn.test", "ADMIN", "ACTIVE"));
        jpaUserRepository.save(userEntity(volunteerId, "Volunteer", "User", "volunteer@add-turn.test", "VOLUNTEER", "ACTIVE"));

        adminToken = tokenService.generateToken(user(adminId, "Admin", "User", "admin@add-turn.test", Role.ADMIN, UserStatus.ACTIVE));
        volunteerToken = tokenService.generateToken(user(volunteerId, "Volunteer", "User", "volunteer@add-turn.test", Role.VOLUNTEER, UserStatus.ACTIVE));
    }

    @Test
    void addTurn_WhenAdminAndValidPayload_ReturnsCreatedAndPersistsTurn() throws Exception {
        String email = "jane@example.com";
        LocalDate date = LocalDate.of(2026, 7, 17);

        mockMvc.perform(post("/api/v1/admin/turns")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addTurnJson(email, date.toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.turnId").isNotEmpty())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.date").value("2026-07-17"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        List<TurnJpaEntity> turns = jpaTurnRepository.findAll();
        assertEquals(1, turns.size());
        TurnJpaEntity saved = turns.getFirst();

        assertAll(
                () -> assertNotNull(saved.getTurnId()),
                () -> assertEquals(email, saved.getEmail()),
                () -> assertEquals(date, saved.getTurnDate()),
                () -> assertNotNull(saved.getCreatedAt())
        );
    }

    @Test
    void addTurn_WhenEmailHasUppercase_NormalizesToLowerCase() throws Exception {
        mockMvc.perform(post("/api/v1/admin/turns")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addTurnJson("Jane@Example.COM", "2026-07-17")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jane@example.com"));

        assertEquals("jane@example.com", jpaTurnRepository.findAll().getFirst().getEmail());
    }

    @Test
    void addTurn_WhenTurnAlreadyExistsForEmailAndDate_ReturnsConflict() throws Exception {
        String email = "jane@example.com";
        LocalDate date = LocalDate.of(2026, 7, 17);
        jpaTurnRepository.save(TurnJpaEntity.create(UUID.randomUUID(), email, date, Instant.now()));

        mockMvc.perform(post("/api/v1/admin/turns")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addTurnJson(email, date.toString())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertEquals(1, jpaTurnRepository.count());
    }

    @Test
    void addTurn_WhenSameEmailDifferentDate_CreatesBothTurns() throws Exception {
        String email = "jane@example.com";
        jpaTurnRepository.save(TurnJpaEntity.create(UUID.randomUUID(), email, LocalDate.of(2026, 7, 17), Instant.now()));

        mockMvc.perform(post("/api/v1/admin/turns")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addTurnJson(email, "2026-07-18")))
                .andExpect(status().isCreated());

        assertEquals(2, jpaTurnRepository.count());
    }

    @Test
    void addTurn_WhenAuthenticatedUserIsNotAdmin_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/admin/turns")
                        .header("Authorization", "Bearer " + volunteerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addTurnJson("jane@example.com", "2026-07-17")))
                .andExpect(status().isForbidden());

        assertEquals(0, jpaTurnRepository.count());
    }

    @Test
    void addTurn_WhenNoBearerToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/admin/turns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addTurnJson("jane@example.com", "2026-07-17")))
                .andExpect(status().isUnauthorized());

        assertEquals(0, jpaTurnRepository.count());
    }

    @Test
    void addTurn_WhenEmailIsInvalid_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/admin/turns")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addTurnJson("not-an-email", "2026-07-17")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertEquals(0, jpaTurnRepository.count());
    }

    @Test
    void addTurn_WhenDateIsMissing_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/admin/turns")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jane@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertEquals(0, jpaTurnRepository.count());
    }

    @Test
    void addTurn_WhenDateFormatIsInvalid_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/admin/turns")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addTurnJson("jane@example.com", "17-07-2026")))
                .andExpect(status().isBadRequest());

        assertEquals(0, jpaTurnRepository.count());
    }

    private String addTurnJson(String email, String date) {
        return "{\"email\":\"" + email + "\",\"date\":\"" + date + "\"}";
    }

    @Test
    void deleteTurn_WhenAdminAndTurnExists_ReturnsNoContentAndRemovesRow() throws Exception {
        UUID turnId = UUID.randomUUID();
        jpaTurnRepository.save(TurnJpaEntity.create(
                turnId,
                "jane@example.com",
                LocalDate.of(2026, 7, 17),
                Instant.now()
        ));

        mockMvc.perform(delete("/api/v1/admin/turns/{turnId}", turnId.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertEquals(0, jpaTurnRepository.count());
    }

    @Test
    void deleteTurn_WhenTurnDoesNotExist_ReturnsNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/turns/{turnId}", unknownId.toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteTurn_WhenTurnIdIsInvalidUuid_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/turns/{turnId}", "not-a-uuid")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteTurn_WhenAuthenticatedUserIsNotAdmin_ReturnsForbidden() throws Exception {
        UUID turnId = UUID.randomUUID();
        jpaTurnRepository.save(TurnJpaEntity.create(
                turnId,
                "jane@example.com",
                LocalDate.of(2026, 7, 17),
                Instant.now()
        ));

        mockMvc.perform(delete("/api/v1/admin/turns/{turnId}", turnId.toString())
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());

        assertEquals(1, jpaTurnRepository.count());
    }

    @Test
    void deleteTurn_WhenNoBearerToken_ReturnsUnauthorized() throws Exception {
        UUID turnId = UUID.randomUUID();
        jpaTurnRepository.save(TurnJpaEntity.create(
                turnId,
                "jane@example.com",
                LocalDate.of(2026, 7, 17),
                Instant.now()
        ));

        mockMvc.perform(delete("/api/v1/admin/turns/{turnId}", turnId.toString()))
                .andExpect(status().isUnauthorized());

        assertEquals(1, jpaTurnRepository.count());
    }

    @Test
    void listTurns_WhenAdminAndNoFilters_ReturnsAllTurnsSortedByDateThenEmail() throws Exception {
        Instant now = Instant.now();
        UUID t1 = UUID.randomUUID();
        UUID t2 = UUID.randomUUID();
        UUID t3 = UUID.randomUUID();
        UUID t4 = UUID.randomUUID();
        // Insert intentionally out of order.
        jpaTurnRepository.save(TurnJpaEntity.create(t1, "zoe@example.com",  LocalDate.of(2026, 7, 18), now));
        jpaTurnRepository.save(TurnJpaEntity.create(t2, "alice@example.com", LocalDate.of(2026, 7, 18), now));
        jpaTurnRepository.save(TurnJpaEntity.create(t3, "bob@example.com",   LocalDate.of(2026, 7, 17), now));
        jpaTurnRepository.save(TurnJpaEntity.create(t4, "alice@example.com", LocalDate.of(2026, 7, 17), now));

        mockMvc.perform(get("/api/v1/admin/turns")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(1))
                // sort: (date asc, email asc)
                .andExpect(jsonPath("$.content[0].date").value("2026-07-17"))
                .andExpect(jsonPath("$.content[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$.content[1].date").value("2026-07-17"))
                .andExpect(jsonPath("$.content[1].email").value("bob@example.com"))
                .andExpect(jsonPath("$.content[2].date").value("2026-07-18"))
                .andExpect(jsonPath("$.content[2].email").value("alice@example.com"))
                .andExpect(jsonPath("$.content[3].date").value("2026-07-18"))
                .andExpect(jsonPath("$.content[3].email").value("zoe@example.com"));
    }

    @Test
    void listTurns_WhenFilteredByEmail_ReturnsOnlyMatchingTurnsSortedByDate() throws Exception {
        Instant now = Instant.now();
        jpaTurnRepository.save(TurnJpaEntity.create(UUID.randomUUID(), "alice@example.com", LocalDate.of(2026, 7, 18), now));
        jpaTurnRepository.save(TurnJpaEntity.create(UUID.randomUUID(), "alice@example.com", LocalDate.of(2026, 7, 17), now));
        jpaTurnRepository.save(TurnJpaEntity.create(UUID.randomUUID(), "bob@example.com",   LocalDate.of(2026, 7, 17), now));

        mockMvc.perform(get("/api/v1/admin/turns")
                        .param("email", "alice@example.com")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$.content[0].date").value("2026-07-17"))
                .andExpect(jsonPath("$.content[1].email").value("alice@example.com"))
                .andExpect(jsonPath("$.content[1].date").value("2026-07-18"));
    }

    @Test
    void listTurns_WhenFilteredByEmailWithMixedCase_MatchesLowerCased() throws Exception {
        jpaTurnRepository.save(TurnJpaEntity.create(UUID.randomUUID(), "alice@example.com", LocalDate.of(2026, 7, 17), Instant.now()));

        mockMvc.perform(get("/api/v1/admin/turns")
                        .param("email", "Alice@Example.COM")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("alice@example.com"));
    }

    @Test
    void listTurns_WhenFilteredByDate_ReturnsOnlyMatchingTurnsSortedByEmail() throws Exception {
        Instant now = Instant.now();
        jpaTurnRepository.save(TurnJpaEntity.create(UUID.randomUUID(), "zoe@example.com",   LocalDate.of(2026, 7, 17), now));
        jpaTurnRepository.save(TurnJpaEntity.create(UUID.randomUUID(), "alice@example.com", LocalDate.of(2026, 7, 17), now));
        jpaTurnRepository.save(TurnJpaEntity.create(UUID.randomUUID(), "bob@example.com",   LocalDate.of(2026, 7, 18), now));

        mockMvc.perform(get("/api/v1/admin/turns")
                        .param("date", "2026-07-17")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$.content[0].date").value("2026-07-17"))
                .andExpect(jsonPath("$.content[1].email").value("zoe@example.com"))
                .andExpect(jsonPath("$.content[1].date").value("2026-07-17"));
    }

    @Test
    void listTurns_WhenFilteredByEmailAndDate_ReturnsExactMatch() throws Exception {
        Instant now = Instant.now();
        jpaTurnRepository.save(TurnJpaEntity.create(UUID.randomUUID(), "alice@example.com", LocalDate.of(2026, 7, 17), now));
        jpaTurnRepository.save(TurnJpaEntity.create(UUID.randomUUID(), "alice@example.com", LocalDate.of(2026, 7, 18), now));
        jpaTurnRepository.save(TurnJpaEntity.create(UUID.randomUUID(), "bob@example.com",   LocalDate.of(2026, 7, 17), now));

        mockMvc.perform(get("/api/v1/admin/turns")
                        .param("email", "alice@example.com")
                        .param("date", "2026-07-17")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$.content[0].date").value("2026-07-17"));
    }

    @Test
    void listTurns_WhenNoTurns_ReturnsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/v1/admin/turns")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void listTurns_WhenPageAndSizeProvided_HonorsPagination() throws Exception {
        Instant now = Instant.now();
        for (int i = 0; i < 5; i++) {
            jpaTurnRepository.save(TurnJpaEntity.create(
                    UUID.randomUUID(),
                    "user" + i + "@example.com",
                    LocalDate.of(2026, 7, 17),
                    now
            ));
        }

        mockMvc.perform(get("/api/v1/admin/turns")
                        .param("page", "1")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].email").value("user2@example.com"))
                .andExpect(jsonPath("$.content[1].email").value("user3@example.com"));
    }

    @Test
    void listTurns_WhenEmailFilterIsInvalid_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/admin/turns")
                        .param("email", "not-an-email")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listTurns_WhenDateFilterFormatIsInvalid_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/admin/turns")
                        .param("date", "17-07-2026")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listTurns_WhenAuthenticatedUserIsNotAdmin_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/turns")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void listTurns_WhenNoBearerToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/turns"))
                .andExpect(status().isUnauthorized());
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
