package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.integration;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenGenerator;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository.JpaRefreshTokenRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository.JpaUserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import jakarta.servlet.http.Cookie;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class RefreshTokenEndpointTestIT {

    private static final String EMAIL = "refresh-user@test.com";
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
                VolunteerID.generate().asString(),
                "Refresh",
                "User",
                EMAIL,
                new BCryptPasswordEncoder().encode(PASSWORD),
                "VOLUNTEER",
                "ACTIVE"
        ));
    }

    @Test
    void refresh_WhenRefreshCookieIsValid_ReturnsNewAccessTokenAndRotatesRefreshToken() throws Exception {
        String loginResponseCookie = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "refresh-user@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.role").value("VOLUNTEER"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.SET_COOKIE);

        String firstRawRefreshToken = extractCookieValue(loginResponseCookie, "refresh_token");
        String firstRefreshTokenHash = refreshTokenGenerator.hash(firstRawRefreshToken).asString();

        Optional<RefreshTokenJpaEntity> storedFirstToken =
                jpaRefreshTokenRepository.findByTokenHash(firstRefreshTokenHash);

        assertTrue(storedFirstToken.isPresent());

        String refreshResponseCookie = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh_token", firstRawRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.role").value("VOLUNTEER"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.SET_COOKIE);

        String secondRawRefreshToken = extractCookieValue(refreshResponseCookie, "refresh_token");
        String secondRefreshTokenHash = refreshTokenGenerator.hash(secondRawRefreshToken).asString();

        RefreshTokenJpaEntity rotatedFirstToken =
                jpaRefreshTokenRepository.findByTokenHash(firstRefreshTokenHash).orElseThrow();
        RefreshTokenJpaEntity storedSecondToken =
                jpaRefreshTokenRepository.findByTokenHash(secondRefreshTokenHash).orElseThrow();

        assertAll(
                () -> assertNotEquals(firstRawRefreshToken, secondRawRefreshToken),
                () -> assertTrue(refreshResponseCookie.contains("HttpOnly")),
                () -> assertTrue(refreshResponseCookie.contains("SameSite=Lax")),
                () -> assertTrue(refreshResponseCookie.contains("Path=/api/v1/auth")),
                () -> assertTrue(refreshResponseCookie.contains("Max-Age=864000")),
                () -> assertTrue(rotatedFirstToken.getRevokedAt() != null),
                () -> assertTrue(rotatedFirstToken.getLastUsedAt() != null),
                () -> assertTrue(rotatedFirstToken.getReplacedBy() != null),
                () -> assertTrue(storedSecondToken.getRevokedAt() == null),
                () -> assertTrue(storedSecondToken.getReplacedBy() == null),
                () -> assertTrue(rotatedFirstToken.getFamilyId().equals(storedSecondToken.getFamilyId())),
                () -> assertTrue(rotatedFirstToken.getReplacedBy().equals(storedSecondToken.getId()))
        );
    }

    @Test
    void refresh_WhenRefreshCookieIsMissing_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
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
