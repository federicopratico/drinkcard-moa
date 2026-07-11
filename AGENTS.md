# AGENTS.md

Operational guide for AI coding agents working in this repository. Read this before making changes.

## Project

- **Name:** DrinkCard MOA — festival drink-card backend
- **Type:** Spring Boot REST API (single Maven module)
- **Purpose:** replaces paper drink vouchers with digital credits, SumUp checkout, and one-use QR drink tickets

See `README.md` for product scope and `documents/documentacion-funcional-app-festival.en.md` for functional docs.

## Tech stack

| Layer         | Choice                                                 |
| ------------- | ------------------------------------------------------ |
| Language      | Java 21                                                |
| Framework     | Spring Boot 3.5.14 (parent POM)                        |
| Build         | Maven (no wrapper — use system `mvn`)                  |
| Persistence   | Spring Data JPA + Hibernate, PostgreSQL 16             |
| Migrations    | Flyway (`src/main/resources/db/migration/V*.sql`)      |
| Security      | Spring Security + JWT (jjwt 0.12.3), stateless         |
| Locking       | `spring-integration-jdbc` distributed lock (`int_lock`)|
| Payments      | SumUp REST API                                         |
| Mail          | Mailtrap (`mailtrap-java` 1.3.0)                       |
| Testing       | JUnit 5, Mockito 5.17, Testcontainers, WireMock 3.10   |
| Lombok        | Yes — expect stale LSP errors about missing getters/finals; trust `mvn compile` |

## Repository layout

```
src/main/java/cat/itacademy/s04/t02/n02/drinkcardmoa/
├── DrinkcardMoaApplication.java   # entry point
├── iam/          # users, auth, JWT, invitations, password reset
├── drinkcard/    # accounts, payments (SumUp), drink tickets
├── turn/         # volunteer shifts (email + date)
├── messaging/    # domain event listeners
└── shared/       # cross-cutting: VolunteerID VO, GlobalExceptionHandler, JpaSpecificationBuilder, DomainException
```

Each bounded context follows **hexagonal / ports-and-adapters**:

```
<context>/
├── domain/
│   ├── model/{aggregate,valueobject}/
│   └── exception/
├── application/
│   ├── port/
│   │   ├── in/{usecase,dto/{command,query,result}}/
│   │   └── out/                     # repository ports, external ports
│   └── service/                     # use case implementations
└── infrastructure/
    ├── adapter/
    │   ├── in/rest/{controller,dto/{request,response},mapper}/
    │   └── out/persistence/{entity,repository,adapter,mapper}/
    └── config/
```

## Architecture rules

- **Domain layer** has no Spring / JPA / Jackson annotations. Pure Java.
- **Application layer** depends only on domain and its own ports. Services live under `application/service/`.
- **Infrastructure adapters** implement ports. JPA entities are separate from domain aggregates; a `*Mapper` (in `persistence/mapper/`) translates between them.
- **REST layer** uses request/response DTOs (records) with Jakarta Bean Validation. A `*ControllerMapper` translates request ↔ command and result ↔ response.
- **Domain exceptions** extend `shared.exception.DomainException` and are mapped to HTTP status in `shared/infrastructure/GlobalExceptionHandler.java`. Register every new domain exception there.
- **Cross-module dependencies** — currently `drinkcard` depends on `iam.UserRepository` (only) and both `drinkcard` and `turn` share `shared.domain.VolunteerID`. Keep new cross-module coupling minimal and always through a port, not an entity.
- **IDs** — `VolunteerID` is a UUID value object in `shared/domain`. `User.id` **is** a `VolunteerID`. Use `VolunteerID.from(String)` at boundaries, `.asString()` when persisting or serializing.
- **Timezone** — the festival runs in Italy. Use `ZoneId.of("Europe/Rome")` whenever computing "today" (see `CreatePaymentCheckoutService`, `AddDrinkCardService`).

## Naming conventions

| Kind                  | Pattern                                    |
| --------------------- | ------------------------------------------ |
| Use case interface    | `<Verb><Noun>UseCase`                      |
| Command               | `<Verb><Noun>Command` (record)             |
| Query                 | `<Verb><Noun>Query` (record)               |
| Result DTO            | `<Verb><Noun>Result` (record)              |
| Service               | `<Verb><Noun>Service implements ...UseCase`|
| Repository port       | `<Aggregate>Repository`                    |
| JPA adapter           | `<Aggregate>JpaAdapter`                    |
| Spring Data repo      | `Jpa<Aggregate>Repository`                 |
| Request DTO           | `<Verb><Noun>Request` (record + validation)|
| Response DTO          | `<Aggregate>Response` (record)             |
| Exception             | `<Reason>Exception extends DomainException`|

## Build & run

```bash
# Compile
mvn compile

# Run all tests (uses Testcontainers — Docker required for ITs)
mvn test

# Run one test class
mvn test -Dtest=AddTurnServiceTest

# Run several
mvn test -Dtest='AddDrinkCardServiceTest,CreatePaymentCheckoutServiceTest'

# Build a jar (skips tests)
mvn -DskipTests package

# Run locally (needs .env — copy from .env.example)
mvn spring-boot:run

# Or via Docker
docker compose up -d postgres    # local Postgres on :5432
mvn spring-boot:run
```

**No Maven wrapper.** Do not `./mvnw` — use system `mvn`.

## Profiles & configuration

Config files under `src/main/resources/`:

- `application.yaml` — base, reads `.env` if present
- `application-dev.yaml` — `ddl-auto: validate`, verbose SQL & security logs (default profile)
- `application-prod.yaml` — production overrides
- `src/test/resources/application-test.yaml` — used by `@ActiveProfiles("test")`

Active profile is set via `SPRING_PROFILES_ACTIVE` (defaults to `dev`).

Environment variables live in `.env` at repo root. See `.env.example` for the full list — includes DB URL, JWT secret, SumUp credentials, Mailtrap credentials, bootstrap admin, and CORS origins.

## Database & migrations

- Postgres 16, JPA `ddl-auto: validate` in all profiles — schema is owned by Flyway, never Hibernate.
- Migrations: `src/main/resources/db/migration/V<n>__<snake_case>.sql`.
- **Never modify a committed migration.** Add a new `V<n+1>__*.sql`.
- Current highest version: `V15__create_turns_table.sql`.
- Shared BIGINT sequence `id_persistence_seq` (allocation size 50) is used by legacy tables. New aggregates use UUID PKs (see `turns`, `drink_tickets`).

## Testing conventions

Three tiers, all under `src/test/java` mirroring `src/main/java` packages:

| Suffix        | Type                | Runs                                    |
| ------------- | ------------------- | --------------------------------------- |
| `*Test.java`  | Unit (Mockito)      | Fast, no Spring context                 |
| `*TestIT.java`| Integration         | `@SpringBootTest` + Testcontainers + real Flyway |
| `*IT.java`    | Integration         | Same as above (both suffixes used)      |

- Unit tests use `@ExtendWith(MockitoExtension.class)` with `@Mock` + `@InjectMocks`, or manual constructor wiring.
- ITs use `@SpringBootTest`, `@Testcontainers`, `@ActiveProfiles("test")`, and a static `@Container PostgreSQLContainer<>("postgres:16-alpine")` with `@DynamicPropertySource` for JDBC properties.
- Controller-flow ITs use `@AutoConfigureMockMvc` and generate real JWTs via the autowired `TokenService`. See `AdminTurnEndpointTestIT` and `AdminPaymentEndpointTestIT` as canonical templates.
- External services (SumUp) are stubbed with **WireMock** (see `CreatePaymentCheckoutServiceIT`).
- Prefer ITs over `@WebMvcTest` slice tests — the project standard is full-stack ITs when covering a controller.
- Every IT must `deleteAll()` on all relevant repositories in `@BeforeEach`.

Surefire is configured with the Mockito Java agent (see `pom.xml` `<argLine>`). Do not remove it.

## Coding standards

- Use `@RequiredArgsConstructor` (Lombok) for services; `@AllArgsConstructor` for adapters and controllers — match existing files in the same directory.
- Aggregates: private constructor + `create(...)` factory + `rehydrate(...)` factory for persistence rehydration + `Objects.requireNonNull` on required fields.
- Value objects: Java records with validation in the compact constructor. Static `from(String)` and `asString()` accessors.
- Timestamps: `java.time.Instant` for machine time, `LocalDate` for date-only fields, always with `Europe/Rome` when converting.
- No `System.out` / `printStackTrace`. Use `Slf4j` via Lombok `@Slf4j` if you need logs.
- Money: `BigDecimal` (see `Card.getPrice()`).
- Never hardcode strings that live in config. Use `@ConfigurationProperties` classes (`PaymentProperties`, etc.).

## Common patterns to reuse

- **Pagination:** `shared.application.dto.PageResult<T>` + `shared.infrastructure.adapter.in.rest.dto.response.PageResponse<T>`.
- **JPA specifications:** `shared.infrastructure.adapter.out.persistence.JpaSpecificationBuilder`.
- **Distributed locking:** `LockRegistry` bean (JDBC lock table `int_lock`, see V10 migration).
- **Global exception handler:** `shared/infrastructure/GlobalExceptionHandler.java` — add a new `@ExceptionHandler` here for every new domain exception.

## Adding a new feature — checklist

1. **Migration:** new `V<n+1>__*.sql` if schema changes.
2. **Domain:** aggregate + value objects + domain exceptions (no framework imports).
3. **Application ports:** `UseCase` interface, `Command`/`Query`, `Result`, repository `port/out` interface.
4. **Application service:** implements the use case, `@Service @RequiredArgsConstructor`, `@Transactional` where needed.
5. **Infrastructure out:** JPA entity, `Jpa<X>Repository`, `<X>JpaAdapter` implementing the port, `<X>Mapper`.
6. **Infrastructure in:** request DTO with validation, response DTO, `<X>ControllerMapper`, controller with `@PreAuthorize` per endpoint.
7. **Exception handler:** register new exceptions in `GlobalExceptionHandler`.
8. **Tests:** unit tests for service + mapper; integration test hitting the endpoint against Testcontainers.
9. **Run** `mvn test` and verify BUILD SUCCESS.

## Deployment

- **Dockerfile** — multi-stage (Maven build → JRE Alpine runtime), non-root `app` user, `JAVA_OPTS` env for JVM tuning.
- **Procfile** — Heroku-style: `web: java -jar target/drinkcard-MOA-0.0.1-SNAPSHOT.jar`.
- **docker-compose.yml** — dev-only Postgres container (`drinkcard-postgres`, volume `drinkcard-postgres-data`).

## Notes for agents

- **LSP noise:** Lombok is heavily used; the editor's LSP frequently reports false "method undefined" or "blank final field may not have been initialized" errors. Trust `mvn compile` and `mvn test` as the source of truth.
- **No `.mvnw`** in this repo — always use system `mvn`.
- **Don't `cd`** — use tool `workdir` parameters.
- **Preserve style** — mirror the closest neighbor file in each directory (imports order, Lombok annotations, constructor patterns).
- **Never edit committed migrations.** Only add new ones.
- **Do not disable or bypass `@PreAuthorize`** to make tests pass — use `TestingAuthenticationToken` with the right role in `@WebMvcTest`, or generate a real JWT for `admin`/`volunteer` in ITs.
- **Do not commit** unless explicitly asked.
