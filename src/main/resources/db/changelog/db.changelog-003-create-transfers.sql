CREATE TABLE transfers (
                           id SERIAL PRIMARY KEY,
                           from_card_id BIGINT NOT NULL,
                           to_card_id BIGINT NOT NULL,
                           amount DECIMAL(19,2) NOT NULL,
                           transfer_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_transfers_from_cards FOREIGN KEY (from_card_id) REFERENCES cards(id),
                           CONSTRAINT fk_transfers_to_cards FOREIGN KEY (to_card_id) REFERENCES cards(id)
);
