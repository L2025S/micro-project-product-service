INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('8f4c1c8d-8b0e-4a0d-9e4a-2c9b1a1f1a11', 'Apple AirPods Pro 2', 'Active noise cancellation wireless earbuds with improved sound quality.', 249.00, 10, NOW(), NOW())
ON CONFLICT (uuid) DO NOTHING;
INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('b2e1f4d3-7c2b-4e8a-9f3d-1a2b3c4d5e66', 'Sony WH-1000XM5', 'Premium over-ear noise-cancelling headphones with long battery life.', 399.00, 10, NOW(), NOW())
ON CONFLICT (uuid) DO NOTHING;
INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('c3d2e1f4-5b6a-4c7d-8e9f-0a1b2c3d4e77', 'Logitech MX Master 3S', 'Ergonomic wireless mouse with MagSpeed scroll wheel and silent clicks.', 99.99, 10, NOW(), NOW())
ON CONFLICT (uuid) DO NOTHING;
INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('d4e3f2c1-6a7b-4d8c-9f0e-1b2c3d4e5f88', 'Nintendo Switch OLED', 'Hybrid gaming console with vibrant OLED display.', 349.00, 10, NOW(), NOW())
ON CONFLICT (uuid) DO NOTHING;
INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('e5f4c3d2-7b8a-4e9d-0f1e-2c3d4e5f6a99', 'Kindle Paperwhite', 'Waterproof e-reader with adjustable warm light.', 159.99, 10, NOW(), NOW())
ON CONFLICT (uuid) DO NOTHING;
INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('f6e5d4c3-8a9b-4f0d-1e2f-3d4e5f6a7b00', 'Anker PowerCore 20000', 'High-capacity portable power bank with fast charging.', 49.99, 10, NOW(), NOW())
ON CONFLICT (uuid) DO NOTHING;
INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('a7b6c5d4-9b0a-4e1d-2f3e-4e5f6a7b8c11', 'Samsung Galaxy Watch 6', 'Smartwatch with advanced health tracking and AMOLED display.', 299.00, 10, NOW(), NOW())
ON CONFLICT (uuid) DO NOTHING;
INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('b8c7d6e5-0a1b-4f2d-3e4f-5f6a7b8c9d22', 'Instant Pot Duo 7-in-1', 'Multi-functional electric pressure cooker for fast meal prep.', 89.99, 10, NOW(), NOW())
ON CONFLICT (uuid) DO NOTHING;
INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('c9d8e7f6-1b2c-4e3d-4f5e-6a7b8c9d0e33', 'Philips Hue Starter Kit', 'Smart LED lighting system with app and voice control.', 129.99, 10, NOW(), NOW())
ON CONFLICT (uuid) DO NOTHING;
INSERT INTO products (uuid, name, description, price, stock, created_at, updated_at)
VALUES ('d0e9f8a7-2c3d-4f4e-5e6f-7b8c9d0e1f44', 'JBL Flip 6', 'Portable waterproof Bluetooth speaker with powerful sound.', 119.95, 10, NOW(), NOW())
ON CONFLICT (uuid) DO NOTHING;
