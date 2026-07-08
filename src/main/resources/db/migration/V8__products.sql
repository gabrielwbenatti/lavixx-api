-- Catálogo de produtos para venda (sem controle de estoque). Espelha `services`.
CREATE TABLE products (
    id         uuid           PRIMARY KEY NOT NULL,
    tenant_id  uuid           NOT NULL,
    name       varchar(150)   NOT NULL,
    price      numeric(10, 2) NOT NULL,
    created_at timestamp      NOT NULL DEFAULT now(),
    updated_at timestamp      NOT NULL DEFAULT now(),
    CONSTRAINT fk_products_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE INDEX idx_products_tenant ON products (tenant_id);

-- Um item da OS passa a poder referenciar um produto (além de, ou em vez de, um serviço).
ALTER TABLE service_order_items ADD COLUMN product_id uuid;
ALTER TABLE service_order_items
    ADD CONSTRAINT fk_soi_product FOREIGN KEY (product_id) REFERENCES products (id);
