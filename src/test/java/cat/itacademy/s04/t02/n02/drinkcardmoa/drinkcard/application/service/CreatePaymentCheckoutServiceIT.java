package cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.service;

import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.command.CreatePaymentCheckoutCommand;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.application.port.in.dto.result.CreatePaymentCheckoutResult;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.CheckoutAlreadyInProgressException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountNotFoundException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.DrinkCardAccountSuspendedException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.PurchaseLimitExceededException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.exception.RefillDisabledException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.Card;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.domain.model.valueobject.PaymentStatus;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.DrinkCardAccountJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository.JpaDrinkCardAccountRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.drinkcard.infrastructure.adapter.out.persistence.repository.JpaPaymentRepository;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class CreatePaymentCheckoutServiceIT {

    private static final String REDIRECT_URL = "http://localhost:3000/payment/success";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("festival_test")
            .withUsername("postgres")
            .withPassword("postgres");

    static WireMockServer wireMock;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("sumup.base-url", () -> "http://localhost:" + wireMock.port());
        registry.add("app.payment.checkout-expiration", () -> "15m");
        registry.add("app.payment.webhook-return-url",
                () -> "http://localhost:8080/api/v1/payments/sumup/webhook");
    }

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(wireMockConfig()
                .dynamicPort()
                .http2PlainDisabled(true)
                .http2TlsDisabled(true));
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @Autowired
    private CreatePaymentCheckoutService service;

    @Autowired
    private JpaDrinkCardAccountRepository jpaDrinkCardAccountRepository;

    @Autowired
    private JpaPaymentRepository jpaPaymentRepository;

    @Autowired
    private LockRegistry lockRegistry;

    private VolunteerID volunteerId;

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
        jpaPaymentRepository.deleteAll();
        jpaDrinkCardAccountRepository.deleteAll();
        volunteerId = VolunteerID.generate();
    }

    @Test
    void execute_WhenAccountIsActiveAndCanPurchase_PersistsPendingPaymentAndReturnsResult() {
        seedActiveAccount(volunteerId, twoDaysAgo());
        stubSumUpCheckoutCreation("checkout-abc", "https://checkout.example.test/abc");

        CreatePaymentCheckoutResult result = service.execute(
                new CreatePaymentCheckoutCommand(
                        volunteerId.asString(),
                        REDIRECT_URL
                )
        );

        assertAll(
                () -> assertEquals("https://checkout.example.test/abc", result.checkoutUrl()),
                () -> assertEquals(PaymentStatus.PENDING.name(), result.status()),
                () -> assertEquals(0, Card.newCard().getPrice().compareTo(result.amount()))
        );

        Optional<PaymentJpaEntity> persisted = jpaPaymentRepository.findByIdempotencyKey(volunteerId.asString() + OffsetDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_DATE));
        assertTrue(persisted.isPresent());
        PaymentJpaEntity entity = persisted.get();
        assertAll(
                () -> assertEquals(volunteerId.value(), entity.getVolunteerId()),
                () -> assertEquals("checkout-abc", entity.getProviderCheckoutId()),
                () -> assertEquals("https://checkout.example.test/abc", entity.getProviderCheckoutUrl()),
                () -> assertEquals(PaymentStatus.PENDING.name(), entity.getStatus()),
                () -> assertEquals(0, Card.newCard().getPrice().compareTo(entity.getAmount()))
        );

        wireMock.verify(1, postRequestedFor(urlEqualTo("/v0.1/checkouts")));
    }

    @Test
    void execute_WhenSameIdempotencyKeyReused_ReturnsSamePaymentAndPersistsOnlyOnce() {
        seedActiveAccount(volunteerId, twoDaysAgo());
        stubSumUpCheckoutCreation("checkout-xyz", "https://checkout.example.test/xyz");

        CreatePaymentCheckoutCommand command = new CreatePaymentCheckoutCommand(
                volunteerId.asString(),
                REDIRECT_URL
        );

        CreatePaymentCheckoutResult firstResult = service.execute(command);
        CreatePaymentCheckoutResult secondResult = service.execute(command);

        assertAll(
                () -> assertEquals(firstResult.paymentId(), secondResult.paymentId()),
                () -> assertEquals(firstResult.checkoutUrl(), secondResult.checkoutUrl()),
                () -> assertEquals(firstResult.status(), secondResult.status())
        );

        // The unique constraint on idempotency_key prevents a duplicate Payment row.
        // Dedup happens in the catch(DataIntegrityViolationException) branch, so the
        // gateway is still called on the retry but only one Payment is persisted.
        assertEquals(1, jpaPaymentRepository.count());
    }

    @Test
    void execute_WhenAccountDoesNotExist_ThrowsAndDoesNotCallGateway() {
        CreatePaymentCheckoutCommand command = new CreatePaymentCheckoutCommand(
                volunteerId.asString(),
                REDIRECT_URL
        );

        assertThrows(DrinkCardAccountNotFoundException.class, () -> service.execute(command));

        assertEquals(0, jpaPaymentRepository.count());
        wireMock.verify(0, postRequestedFor(urlEqualTo("/v0.1/checkouts")));
    }

    @Test
    void execute_WhenAccountIsSuspended_ThrowsAndDoesNotCallGateway() {
        DrinkCardAccountJpaEntity account = DrinkCardAccountJpaEntity.create(
                volunteerId.asString(),
                0,
                twoDaysAgo(),
                Instant.now(),
                "SUSPENDED"
        );
        jpaDrinkCardAccountRepository.save(account);

        CreatePaymentCheckoutCommand command = new CreatePaymentCheckoutCommand(
                volunteerId.asString(),
                REDIRECT_URL
        );

        assertThrows(RefillDisabledException.class, () -> service.execute(command));

        assertEquals(0, jpaPaymentRepository.count());
        wireMock.verify(0, postRequestedFor(urlEqualTo("/v0.1/checkouts")));
    }

    @Test
    void execute_WhenAccountAlreadyPurchasedToday_ThrowsAndDoesNotCallGateway() {
        seedActiveAccount(volunteerId, Instant.now());

        CreatePaymentCheckoutCommand command = new CreatePaymentCheckoutCommand(
                volunteerId.asString(),
                REDIRECT_URL
        );

        assertThrows(PurchaseLimitExceededException.class, () -> service.execute(command));

        assertEquals(0, jpaPaymentRepository.count());
        wireMock.verify(0, postRequestedFor(urlEqualTo("/v0.1/checkouts")));
    }

    @Test
    void execute_WhenAnotherCheckoutIsInProgressForSameVolunteer_ThrowsCheckoutAlreadyInProgress() throws Exception {
        seedActiveAccount(volunteerId, twoDaysAgo());

        String lockKey = volunteerId.asString()
                + OffsetDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_DATE);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        executor.submit(() -> {
            Lock heldLock = lockRegistry.obtain(lockKey);
            if (!heldLock.tryLock()) {
                throw new IllegalStateException("Holder thread could not acquire the lock");
            }
            try {
                acquired.countDown();
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                heldLock.unlock();
            }
        });

        try {
            assertTrue(acquired.await(5, TimeUnit.SECONDS), "Holder thread did not acquire the lock");

            CreatePaymentCheckoutCommand command = new CreatePaymentCheckoutCommand(
                    volunteerId.asString(),
                    REDIRECT_URL
            );

            assertThrows(CheckoutAlreadyInProgressException.class, () -> service.execute(command));

            assertEquals(0, jpaPaymentRepository.count());
            wireMock.verify(0, postRequestedFor(urlEqualTo("/v0.1/checkouts")));
        } finally {
            release.countDown();
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void seedActiveAccount(VolunteerID volunteerId, Instant lastPurchase) {
        DrinkCardAccountJpaEntity account = DrinkCardAccountJpaEntity.create(
                volunteerId.asString(),
                0,
                lastPurchase,
                Instant.now(),
                "ACTIVE"
        );
        jpaDrinkCardAccountRepository.save(account);
    }

    private void stubSumUpCheckoutCreation(String providerCheckoutId, String checkoutUrl) {
        wireMock.stubFor(post(urlEqualTo("/v0.1/checkouts"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": "%s",
                                  "status": "PENDING",
                                  "hosted_checkout_url": "%s",
                                  "date": "2025-01-01T12:00:00Z"
                                }
                                """.formatted(providerCheckoutId, checkoutUrl))));
    }

    private static Instant twoDaysAgo() {
        return Instant.now().minus(2, ChronoUnit.DAYS);
    }
}
