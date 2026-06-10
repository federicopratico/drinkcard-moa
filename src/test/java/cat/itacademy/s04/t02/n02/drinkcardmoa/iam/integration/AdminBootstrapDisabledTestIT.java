package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.integration;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "app.bootstrap-admin.enabled=false",
                "app.bootstrap-admin.email=disabled-bootstrap-admin@test.com",
                "app.bootstrap-admin.password=SecurePassword123!",
                "app.bootstrap-admin.first-name=Disabled",
                "app.bootstrap-admin.last-name=Admin"
        }
)
@Testcontainers
@ActiveProfiles("test")
class AdminBootstrapDisabledTestIT {

    private static final String BOOTSTRAP_ADMIN_EMAIL = "disabled-bootstrap-admin@test.com";

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
    private JpaUserRepository jpaUserRepository;

    @Test
    void applicationStartup_WhenBootstrapAdminDisabled_DoesNotCreateBootstrapAdmin() {
        var admin = jpaUserRepository.findByEmail(BOOTSTRAP_ADMIN_EMAIL);

        assertFalse(admin.isPresent());
    }
}