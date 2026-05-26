
CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          uuid VARCHAR(36) NOT NULL UNIQUE,
                          name VARCHAR(400) NOT NULL,
                          description VARCHAR(1000),
                          price NUMERIC(12, 2) NOT NULL,
                          stock INTEGER NOT NULL,
                          created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);



INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('a1f3c2b0-1e23-4c9a-9f11-111111111111', 'Wireless Mouse', 'Ergonomic wireless mouse with adjustable DPI.', 19.99, 120, NOW(), NOW());

INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('b2e4d3c1-2f34-5d0b-a022-222222222222', 'Mechanical Keyboard', 'RGB backlit mechanical keyboard with blue switches.', 79.90, 80, NOW(), NOW());

INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('c3f5e4d2-3a45-6e1c-b133-333333333333', 'USB-C Charger', 'Fast charging 30W USB‑C wall charger.', 24.50, 200, NOW(), NOW());

INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('d4a6f5e3-4b56-7f2d-c244-444444444444', 'Noise Cancelling Headphones', 'Over‑ear headphones with ANC and 30h battery.', 129.00, 45, NOW(), NOW());

INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('e5b7a6f4-5c67-8a3e-d355-555555555555', '4K Monitor', '27‑inch 4K UHD IPS display.', 299.99, 30, NOW(), NOW());

INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('f6c8b7a5-6d78-9b4f-e466-666666666666', 'Bluetooth Speaker', 'Portable speaker with deep bass and 12h playtime.', 49.95, 150, NOW(), NOW());

INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('a7d9c8b6-7e89-0c5a-f577-777777777777', 'Webcam 1080p', 'Full HD webcam with autofocus and noise‑reducing mic.', 39.99, 90, NOW(), NOW());

INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('b8e0d9c7-8f90-1d6b-a688-888888888888', 'Portable SSD 1TB', 'High‑speed NVMe portable SSD.', 119.00, 60, NOW(), NOW());

INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('c9f1e0d8-9a01-2e7c-b799-999999999999', 'Gaming Chair', 'Ergonomic gaming chair with lumbar support.', 189.50, 25, NOW(), NOW());

INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('da02f1e9-0b12-3f8d-c8aa-aaaaaaaaaaaa', 'Smart LED Light Strip', 'RGB LED strip with app control.', 29.90, 140, NOW(), NOW());