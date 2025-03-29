-- init-order.sql
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    order_date TIMESTAMP,
    status VARCHAR(50),
    total_amount DECIMAL(10, 2),
    payment_id UUID,
    products JSONB
    );

INSERT INTO orders (id, order_date, status, total_amount, payment_id, products)
VALUES
    ('550e8400-e29b-41d4-a716-446655440000', '2025-03-28 10:00:00', 'PENDING', 150.50, '550e8400-e29b-41d4-a716-446655440001', '["550e8400-e29b-41d4-a716-446655440002", "550e8400-e29b-41d4-a716-446655440003"]'),
    ('550e8400-e29b-41d4-a716-446655440004', '2025-03-28 11:00:00', 'DELIVERED', 200.75, '550e8400-e29b-41d4-a716-446655440005', '["550e8400-e29b-41d4-a716-446655440006"]');