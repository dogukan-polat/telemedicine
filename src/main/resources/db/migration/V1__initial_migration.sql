CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users
(
    id           UUID                  DEFAULT uuid_generate_v4() PRIMARY KEY,
    email        VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(50)  NOT NULL,
    first_name   VARCHAR(50)  NOT NULL,
    last_name    VARCHAR(50)  NOT NULL,
    phone_number VARCHAR(20),
    is_active    BOOLEAN               DEFAULT TRUE,
    created_at   DATE         NOT NULL DEFAULT CURRENT_DATE,
    updated_at   DATE         NOT NULL DEFAULT CURRENT_DATE
);