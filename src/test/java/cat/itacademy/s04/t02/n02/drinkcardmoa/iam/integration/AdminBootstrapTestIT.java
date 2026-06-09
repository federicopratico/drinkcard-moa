package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.integration;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
class AdminBootstrapTestIT {

    private static final String ADMIN_EMAIL = "bootstrap-admin@test.com";
    private static final String ADMIN_PASSWORD = "SecurePassword123!";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("festival_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("app.bootstrap-admin.enabled", () -> "true");
        registry.add("app.bootstrap-admin.email", () -> ADMIN_EMAIL);
        registry.add("app.bootstrap-admin.password", () -> ADMIN_PASSWORD);
        registry.add("app.bootstrap-admin.first-name", () -> "Bootstrap");
        registry.add("app.bootstrap-admin.last-name", () -> "Admin");
    }

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Test
    void applicationStartup_WhenBootstrapAdminEnabled_CreatesAdminUser() {
        var admin = jpaUserRepository.findByEmail(ADMIN_EMAIL);

        assertTrue(admin.isPresent());
        assertEquals("Bootstrap", admin.get().getFirstName());
        assertEquals("Admin", admin.get().getLastName());
        assertEquals("ADMIN", admin.get().getRole());
        assertEquals("ACTIVE", admin.get().getStatus());

        assertNotEquals(ADMIN_PASSWORD, admin.get().getPassword());
        assertTrue(new BCryptPasswordEncoder().matches(ADMIN_PASSWORD, admin.get().getPassword()));
    }
}