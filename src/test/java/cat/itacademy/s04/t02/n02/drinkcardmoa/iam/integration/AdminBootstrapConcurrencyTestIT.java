package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "app.bootstrap-admin.enabled=false"
)
@Testcontainers
@ActiveProfiles("test")
class AdminBootstrapConcurrencyTestIT {

    private static final int THREADS = 100;
    private static final String EMAIL = "concurrent-admin@test.com";

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM users WHERE email = ?", EMAIL);
    }

    @Test
    void uniqueEmailConstraint_AllowsOnlyOneConcurrentInsert() throws Exception {
        var ready = new CountDownLatch(THREADS);
        var start = new CountDownLatch(1);
        var successfulInserts = new AtomicInteger();
        var rejectedInserts = new AtomicInteger();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, THREADS)
                    .mapToObj(index -> executor.submit(() -> {
                        ready.countDown();
                        start.await();

                        try {
                            jdbcTemplate.update("""
                                    INSERT INTO users (
                                        id, first_name, last_name,
                                        email, password, role, status
                                    )
                                    VALUES (?, ?, ?, ?, ?, ?, ?)
                                    """,
                                    UUID.randomUUID().toString(),
                                    "System",
                                    "Admin",
                                    EMAIL,
                                    "hashed-password",
                                    "ADMIN",
                                    "ACTIVE"
                            );

                            successfulInserts.incrementAndGet();
                        } catch (DuplicateKeyException exception) {
                            rejectedInserts.incrementAndGet();
                        }

                        return null;
                    }))
                    .toList();

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            for (Future<?> future : futures) {
                future.get();
            }
        }

        Integer storedUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?",
                Integer.class,
                EMAIL
        );

        assertAll(
                () -> assertEquals(1, successfulInserts.get()),
                () -> assertEquals(THREADS - 1, rejectedInserts.get()),
                () -> assertEquals(1, storedUsers)
        );
    }
}
