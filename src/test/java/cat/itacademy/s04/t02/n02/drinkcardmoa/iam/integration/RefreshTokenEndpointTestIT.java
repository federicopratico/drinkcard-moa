package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.integration;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenGenerator;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.LoginResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.RefreshTokenResponse;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository.JpaRefreshTokenRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.persistence.repository.JpaUserRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
    @Autowired
    private ObjectMapper objectMapper;

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
    void refresh_WhenRefreshRefreshTokenIsValid_ReturnsNewAccessTokenAndRotatesRefreshToken() throws Exception {
        var loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "refresh-user@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.role").value("VOLUNTEER"))
                .andReturn()
                .getResponse();


        var result = objectMapper.readValue(loginResponse.getContentAsString(), LoginResponse.class);
        String firstRawRefreshToken = result.refreshToken();
        String firstRefreshTokenHash = refreshTokenGenerator.hash(firstRawRefreshToken).asString();

        Optional<RefreshTokenJpaEntity> storedFirstToken =
                jpaRefreshTokenRepository.findByTokenHash(firstRefreshTokenHash);

        assertTrue(storedFirstToken.isPresent());

        var refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(firstRawRefreshToken))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.role").value("VOLUNTEER"))
                .andReturn()
                .getResponse();

        var refreshResult = objectMapper.readValue(refreshResponse.getContentAsString(), RefreshTokenResponse.class);
        String secondRawRefreshToken = refreshResult.refreshToken();
        String secondRefreshTokenHash = refreshTokenGenerator.hash(secondRawRefreshToken).asString();

        RefreshTokenJpaEntity rotatedFirstToken =
                jpaRefreshTokenRepository.findByTokenHash(firstRefreshTokenHash).orElseThrow();
        RefreshTokenJpaEntity storedSecondToken =
                jpaRefreshTokenRepository.findByTokenHash(secondRefreshTokenHash).orElseThrow();

        assertAll(
                () -> assertNotEquals(firstRawRefreshToken, secondRawRefreshToken),
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
    void refresh_WhenRefreshIsMissing_ReturnsUBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

}
