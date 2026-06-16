package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.integration;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenGenerator;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository.JpaRefreshTokenRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository.JpaUserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class LoginEndpointTestIT {

    private static final String USER_ID = "4f0a8db1-63a7-4997-944c-9f2f6b82e6d1";
    private static final String EMAIL = "login-user@test.com";
    private static final String PASSWORD = "password123";

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
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Autowired
    private RefreshTokenGenerator refreshTokenGenerator;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jpaRefreshTokenRepository.deleteAll();
        jpaUserRepository.deleteAll();

        jpaUserRepository.save(UserJpaEntity.create(
                USER_ID,
                "Login",
                "User",
                EMAIL,
                new BCryptPasswordEncoder().encode(PASSWORD),
                "VOLUNTEER",
                "ACTIVE"
        ));
    }

    @Test
    void login_WhenCredentialsAreValid_ReturnsAccessTokenAndRefreshCookie() throws Exception {
        String setCookie = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "login-user@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.volunteerId").value(USER_ID))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.role").value("VOLUNTEER"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.SET_COOKIE);

        String rawRefreshToken = extractCookieValue(setCookie, "refresh_token");
        String refreshTokenHash = refreshTokenGenerator.hash(rawRefreshToken).asString();

        Optional<RefreshTokenJpaEntity> storedRefreshToken =
                jpaRefreshTokenRepository.findByTokenHash(refreshTokenHash);

        assertAll(
                () -> assertTrue(setCookie.contains("refresh_token=")),
                () -> assertTrue(setCookie.contains("HttpOnly")),
                () -> assertTrue(setCookie.contains("SameSite=Lax")),
                () -> assertTrue(setCookie.contains("Path=/api/v1/auth")),
                () -> assertTrue(setCookie.contains("Max-Age=864000")),
                () -> assertTrue(storedRefreshToken.isPresent()),
                () -> assertEquals(USER_ID, storedRefreshToken.orElseThrow().getUserId()),
                () -> assertTrue(storedRefreshToken.orElseThrow().getRevokedAt() == null),
                () -> assertTrue(storedRefreshToken.orElseThrow().getReplacedBy() == null)
        );
    }

    @Test
    void login_WhenPasswordIsInvalid_ReturnsUnauthorizedAndDoesNotCreateRefreshToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "login-user@test.com",
                                  "password": "wrongpass"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        assertTrue(jpaRefreshTokenRepository.findAll().isEmpty());
    }

    private String extractCookieValue(String setCookieHeader, String cookieName) {
        String prefix = cookieName + "=";

        for (String part : setCookieHeader.split(";")) {
            String trimmedPart = part.trim();

            if (trimmedPart.startsWith(prefix)) {
                return trimmedPart.substring(prefix.length());
            }
        }

        throw new AssertionError("Cookie not found: " + cookieName);
    }
}
