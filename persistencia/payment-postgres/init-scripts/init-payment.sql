-- init-payment.sql
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    order_id UUID,
    payment_method VARCHAR(50),
    payment_status VARCHAR(50),
    amount DECIMAL(10, 2),
    payment_date TIMESTAMP
    );

INSERT INTO payments (id, order_id, payment_method, payment_status, amount, payment_date)
VALUES
    ('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'CREDIT_CARD', 'COMPLETED', 150.50, '2025-03-28 10:05:00'),
    ('550e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440004', 'PAYPAL', 'COMPLETED', 200.75, '2025-03-28 11:05:00');