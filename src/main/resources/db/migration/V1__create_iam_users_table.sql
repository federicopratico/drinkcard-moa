CREATE TABLE users(
    id VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

INSERT INTO users (id, first_name, last_name, email, password, role)
VALUES ('8799df50-d517-4693-9e46-51b537c305a2',
        'System',
        'Admin',
        'admin@drinkcard.local',
        '$2a$15$R8.9MKUADrMt8zbNAETFLOqwD3I7.bMoYCH4qaj8xOLOB/1LzL9ym',
        'ADMIN');