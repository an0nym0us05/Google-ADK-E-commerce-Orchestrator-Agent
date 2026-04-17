-- orders: core order table; items stored as a JSON text array
CREATE TABLE orders (
    id                VARCHAR(50)       PRIMARY KEY,
    customer_id       VARCHAR(50)       NOT NULL,
    status            VARCHAR(50)       NOT NULL,
    items             TEXT              NOT NULL,
    total             DOUBLE PRECISION  NOT NULL,
    created_at        DATE              NOT NULL,
    estimated_delivery DATE
);

CREATE TABLE products (
    id             VARCHAR(50)       PRIMARY KEY,
    name           VARCHAR(200)      NOT NULL,
    description    TEXT,
    price          DOUBLE PRECISION  NOT NULL,
    stock_quantity INTEGER           NOT NULL,
    category       VARCHAR(100)
);

CREATE TABLE refunds (
    id          VARCHAR(50) PRIMARY KEY,
    order_id    VARCHAR(50) NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    status      VARCHAR(50) NOT NULL,
    reason      TEXT,
    created_at  DATE        NOT NULL,
    CONSTRAINT fk_refunds_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Indexes for the two most common query patterns
CREATE INDEX idx_orders_customer_id   ON orders(customer_id);
CREATE INDEX idx_refunds_customer_id  ON refunds(customer_id);
CREATE INDEX idx_refunds_order_id     ON refunds(order_id);
