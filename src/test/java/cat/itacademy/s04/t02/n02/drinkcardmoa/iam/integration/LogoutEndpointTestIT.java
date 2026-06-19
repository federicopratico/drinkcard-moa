package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.integration;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.RefreshTokenGenerator;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.in.rest.dto.response.LoginResponse;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class LogoutEndpointTestIT {

    private static final String EMAIL = "logout-user@test.com";
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
    private ObjectMapper objectMapper;

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
                "Logout",
                "User",
                EMAIL,
                new BCryptPasswordEncoder().encode(PASSWORD),
                "VOLUNTEER",
                "ACTIVE"
        ));
    }

    @Test
    void logout_WhenRefreshTokenIsValid_ReturnsNoContentAndRevokesTokenFamily() throws Exception {
        LoginResponse loginResponse = login();
        String refreshTokenHash = refreshTokenGenerator.hash(loginResponse.refreshToken()).asString();
        RefreshTokenJpaEntity storedToken =
                jpaRefreshTokenRepository.findByTokenHash(refreshTokenHash).orElseThrow();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(loginResponse.refreshToken())))
                .andExpect(status().isNoContent());

        List<RefreshTokenJpaEntity> tokensInFamily = jpaRefreshTokenRepository.findAll()
                .stream()
                .filter(token -> token.getFamilyId().equals(storedToken.getFamilyId()))
                .toList();

        RefreshTokenJpaEntity loggedOutToken =
                jpaRefreshTokenRepository.findByTokenHash(refreshTokenHash).orElseThrow();

        assertAll(
                () -> assertTrue(loggedOutToken.getRevokedAt() != null),
                () -> assertTrue(tokensInFamily.stream().allMatch(token -> token.getRevokedAt() != null))
        );

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(loginResponse.refreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void logout_WhenRefreshTokenIsBlank_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": " " }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private LoginResponse login() throws Exception {
        var response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "logout-user@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse();

        return objectMapper.readValue(response.getContentAsString(), LoginResponse.class);
    }
}
