CREATE TABLE service_order_items (
    id               uuid           PRIMARY KEY NOT NULL,
    tenant_id        uuid           NOT NULL,
    service_order_id uuid           NOT NULL,
    service_id       uuid,
    name             varchar(150)   NOT NULL,
    unit_price       numeric(10, 2) NOT NULL,
    discount         numeric(10, 2) NOT NULL DEFAULT 0,
    quantity         smallint       NOT NULL DEFAULT 1,
    created_at       timestamp      NOT NULL DEFAULT now(),
    updated_at       timestamp      NOT NULL DEFAULT now(),
    CONSTRAINT fk_soi_tenant        FOREIGN KEY (tenant_id)        REFERENCES tenants        (id),
    CONSTRAINT fk_soi_service_order FOREIGN KEY (service_order_id) REFERENCES service_orders (id),
    CONSTRAINT fk_soi_service       FOREIGN KEY (service_id)       REFERENCES services       (id)
);

CREATE INDEX idx_soi_service_order ON service_order_items (service_order_id);
CREATE INDEX idx_soi_tenant_so     ON service_order_items (tenant_id, service_order_id);

-- Migra dados existentes: cada ordem vira um item
INSERT INTO service_order_items (id, tenant_id, service_order_id, service_id, name, unit_price, discount, quantity)
SELECT gen_random_uuid(), so.tenant_id, so.id, so.service_id, s.name, so.price, 0, 1
FROM service_orders so
JOIN services s ON s.id = so.service_id;

ALTER TABLE service_orders DROP CONSTRAINT fk_service_orders_service;
ALTER TABLE service_orders DROP COLUMN service_id;
ALTER TABLE service_orders DROP COLUMN price;
