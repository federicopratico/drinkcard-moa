package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.e2e;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AdminUsersE2ETest {

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

    private String adminToken;
    private String volunteerToken;
    private String adminId;
    private String volunteerId;

    @BeforeEach
    void setUp() {
        jpaUserRepository.deleteAll();

        adminId = VolunteerID.generate().asString();
        volunteerId = VolunteerID.generate().asString();

        jpaUserRepository.save(userEntity(
                adminId,
                "Admin",
                "User",
                "admin@email.com",
                "ADMIN",
                "ACTIVE"
        ));
        jpaUserRepository.save(userEntity(
                volunteerId,
                "Volunteer",
                "User",
                "volunteer@email.com",
                "VOLUNTEER",
                "ACTIVE"
        ));
        jpaUserRepository.save(userEntity(
                VolunteerID.generate().asString(),
                "Suspended",
                "Volunteer",
                "suspended@email.com",
                "VOLUNTEER",
                "SUSPENDED"
        ));

        adminToken = tokenService.generateToken(user(adminId, "Admin", "User", "admin@email.com", Role.ADMIN, UserStatus.ACTIVE));
        volunteerToken = tokenService.generateToken(user(volunteerId, "Volunteer", "User", "volunteer@email.com", Role.VOLUNTEER, UserStatus.ACTIVE));
    }

    @Test
    void listUsers_WhenAdminAndNoFilters_ReturnsAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder(
                        "admin@email.com",
                        "volunteer@email.com",
                        "suspended@email.com"
                )));
    }

    @Test
    void listUsers_WhenFilteredByRole_ReturnsMatchingUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("role", "VOLUNTEER")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder(
                        "volunteer@email.com",
                        "suspended@email.com"
                )))
                .andExpect(jsonPath("$[*].role", containsInAnyOrder(
                        "VOLUNTEER",
                        "VOLUNTEER"
                )));
    }

    @Test
    void listUsers_WhenFilteredByStatus_ReturnsMatchingUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("status", "SUSPENDED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("suspended@email.com"))
                .andExpect(jsonPath("$[0].status").value("SUSPENDED"));
    }

    @Test
    void listUsers_WhenFilteredByEmail_ReturnsMatchingUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("email", "admin@email.com")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("admin@email.com"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void listUsers_WhenFiltersAreCombined_ReturnsMatchingUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("role", "VOLUNTEER")
                        .param("status", "ACTIVE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("volunteer@email.com"))
                .andExpect(jsonPath("$[0].role").value("VOLUNTEER"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void listUsers_WhenAuthenticatedUserIsNotAdmin_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsers_WhenNoBearerToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById_WhenAdminAndUserExists_ReturnsUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/{userId}", volunteerId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(volunteerId))
                .andExpect(jsonPath("$.fullName").value("Volunteer User"))
                .andExpect(jsonPath("$.email").value("volunteer@email.com"))
                .andExpect(jsonPath("$.role").value("VOLUNTEER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getUserById_WhenAdminAndUserDoesNotExist_ReturnsNotFound() throws Exception {
        String missingUserId = VolunteerID.generate().asString();

        mockMvc.perform(get("/api/v1/admin/users/{userId}", missingUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with id: " + missingUserId));
    }

    @Test
    void getUserById_WhenAuthenticatedUserIsNotAdmin_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/{userId}", adminId)
                        .header("Authorization", "Bearer " + volunteerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_WhenNoBearerToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/{userId}", adminId))
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
