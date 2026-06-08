-- Spring Integration JDBC distributed lock registry table.
-- Schema taken from spring-integration-jdbc 6.5.x (schema-postgresql.sql).
-- Used by DefaultLockRepository / JdbcLockRegistry to coordinate exclusive
-- access across multiple application instances.
CREATE TABLE INT_LOCK (
    LOCK_KEY     CHAR(36)     NOT NULL,
    REGION       VARCHAR(100) NOT NULL,
    CLIENT_ID    CHAR(36),
    CREATED_DATE TIMESTAMP    NOT NULL,
    CONSTRAINT INT_LOCK_PK PRIMARY KEY (LOCK_KEY, REGION)
);
