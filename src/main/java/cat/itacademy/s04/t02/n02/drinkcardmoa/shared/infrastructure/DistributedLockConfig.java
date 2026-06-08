package cat.itacademy.s04.t02.n02.drinkcardmoa.shared.infrastructure;

import java.time.Duration;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;
import org.springframework.integration.support.locks.LockRegistry;

/**
 * Spring Integration JDBC-based distributed locks.
 *
 * <p>Backed by the {@code INT_LOCK} table (see migration {@code V10__create_int_lock_table.sql}).
 * A {@link JdbcLockRegistry} hands out {@link java.util.concurrent.locks.Lock} instances that are
 * coordinated through the database, so that scheduled jobs / one-off tasks can be guarded against
 * concurrent execution across multiple application instances.
 *
 * @see <a href="https://docs.spring.io/spring-integration/reference/distributed-locks.html">Spring
 *     Integration: Distributed Locks</a>
 * @see <a href="https://docs.spring.io/spring-integration/reference/jdbc/lock-registry.html">Spring
 *     Integration: JDBC Lock Registry</a>
 */
@Configuration
@ConfigurationProperties(prefix = "app.lock")
public class DistributedLockConfig {

    /** Region used to scope rows in the {@code INT_LOCK} table. */
    private String region = "DEFAULT";

    /**
     * Maximum lifetime of a lock row before another instance is allowed to take it over. Acts as a
     * deadlock breaker if a holder dies without releasing.
     */
    private Duration timeToLive = Duration.ofSeconds(30);

    /** Idle interval between attempts to acquire/insert the lock row. */
    private Duration idleBetweenTries = Duration.ofMillis(100);

    @Bean
    public LockRepository lockRepository(DataSource dataSource) {
        DefaultLockRepository repository = new DefaultLockRepository(dataSource);
        repository.setTimeToLive(Math.toIntExact(timeToLive.toMillis()));
        repository.setRegion(region);
        return repository;
    }

    @Bean
    public LockRegistry lockRegistry(LockRepository lockRepository) {
        JdbcLockRegistry registry = new JdbcLockRegistry(lockRepository);
        registry.setIdleBetweenTries(idleBetweenTries);
        return registry;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Duration getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(Duration timeToLive) {
        this.timeToLive = timeToLive;
    }

    public Duration getIdleBetweenTries() {
        return idleBetweenTries;
    }

    public void setIdleBetweenTries(Duration idleBetweenTries) {
        this.idleBetweenTries = idleBetweenTries;
    }
}
