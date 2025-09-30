CREATE TYPE card_status AS ENUM ('ACTIVE','BLOCKED','EXPIRED');
CREATE TABLE cards (
                       id SERIAL PRIMARY KEY,
                       number VARCHAR(255) NOT NULL UNIQUE,
                       status card_status NOT NULL,
                       balance DECIMAL(19,2) DEFAULT 0,
                       user_id BIGINT NOT NULL,
                       expiry_date DATE NOT NULL,
                       CONSTRAINT fk_cards_users FOREIGN KEY (user_id) REFERENCES users(id)
);

