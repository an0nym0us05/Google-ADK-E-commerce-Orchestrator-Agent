INSERT INTO orders (id, customer_id, status, items, total, created_at, estimated_delivery) VALUES
('ORD-001', 'CUST-001', 'SHIPPED',    '["Wireless Headphones","USB-C Cable"]',    129.99, '2026-03-15', '2026-04-01'),
('ORD-002', 'CUST-001', 'DELIVERED',  '["Running Shoes"]',                          89.99, '2026-03-01', '2026-03-10'),
('ORD-003', 'CUST-002', 'PROCESSING', '["Laptop Stand","Keyboard"]',                74.50, '2026-04-10', '2026-04-20'),
('ORD-004', 'CUST-002', 'CANCELLED',  '["Smartwatch"]',                            199.00, '2026-03-20', '2026-03-30'),
('ORD-005', 'CUST-003', 'DELIVERED',  '["Coffee Maker","Coffee Beans"]',             55.00, '2026-02-10', '2026-02-20');

INSERT INTO products (id, name, description, price, stock_quantity, category) VALUES
('PROD-001', 'Wireless Headphones Pro',    'Premium over-ear noise-cancelling headphones', 89.99,  15,  'Electronics'),
('PROD-002', 'USB-C Cable 2m',             'Durable braided USB-C charging cable',          12.99, 100, 'Accessories'),
('PROD-003', 'Running Shoes X1',           'Lightweight performance running shoes',         79.99,  30,  'Footwear'),
('PROD-004', 'Laptop Stand Adjustable',    'Ergonomic aluminium laptop stand',              39.99,  50,  'Accessories'),
('PROD-005', 'Mechanical Keyboard TKL',    'Tenkeyless mechanical keyboard',                69.99,  20,  'Electronics'),
('PROD-006', 'Smartwatch Series 3',        'Advanced fitness and lifestyle smartwatch',    199.99,   8,  'Electronics'),
('PROD-007', 'BassBoost Headphones X1',    'Deep bass over-ear headphones',                59.99,   8,  'Electronics'),
('PROD-008', 'Coffee Maker Drip 12-Cup',   'Programmable drip coffee maker',               49.99,  25,  'Kitchen'),
('PROD-009', 'Premium Coffee Beans 1kg',   'Single-origin Arabica coffee beans',           18.99,  60,  'Kitchen'),
('PROD-010', 'Portable Bluetooth Speaker', 'Waterproof portable speaker',                  44.99,   0,  'Electronics');

INSERT INTO refunds (id, order_id, customer_id, status, reason, created_at) VALUES
('REF-001', 'ORD-002', 'CUST-001', 'COMPLETED', 'Item damaged on arrival', '2026-03-10'),
('REF-002', 'ORD-004', 'CUST-002', 'COMPLETED', 'Changed my mind',         '2026-03-22'),
('REF-003', 'ORD-005', 'CUST-003', 'PENDING',   'Wrong item received',     '2026-02-17');
