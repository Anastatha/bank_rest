-- liquibase formatted sql

-- changeset bank-system:7
-- Initial admin user (password: admin123)
-- Initial user (password: nastya1152ty)
INSERT INTO users (email, username, password, role) VALUES
                                                        ('admin@gmail.com', 'admin', '$2a$10$3CwNeTYzWKq43ylpmlwqs.LUQw1ObEDKEqHOx/wczz7KT85KjZz9y', 'ADMIN'),
                                                        ('nastya1152ty@gmail.com', 'Nastya', '$2a$10$QUFTNnms2bOiKTJeTyGXhOFgwZKkFR3fiV1u6EIGvugJ422arcoY.', 'USER')
    ON CONFLICT (email) DO NOTHING;

-- changeset bank-system:8
-- Sample cards for users
INSERT INTO cards (number, status, balance, user_id, expiry_date) VALUES
                                                                      ('mpw+UwXYiyTt+6bkatRw1Ef8H+UH2Dvg5SojEpUrEjk=', 'ACTIVE', 1000.00, 2, '2028-09-30'),
                                                                      ('sY60+r2NDrAzFpsyhFAqBkf8H+UH2Dvg5SojEpUrEjk=', 'ACTIVE', 500.00, 2, '2028-09-30'),
                                                                      ('CU6rGy1kaG/VvPtt57UyeUf8H+UH2Dvg5SojEpUrEjk=', 'BLOCKED', 0.00, 2, '2025-12-31')
    ON CONFLICT (number) DO NOTHING;