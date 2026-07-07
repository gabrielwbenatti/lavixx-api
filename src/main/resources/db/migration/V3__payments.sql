CREATE TABLE payment_methods (
    id         uuid         PRIMARY KEY NOT NULL,
    tenant_id  uuid         NOT NULL,
    name       varchar(80)  NOT NULL,
    is_active  boolean      NOT NULL DEFAULT true,
    created_at timestamp    NOT NULL DEFAULT now(),
    updated_at timestamp    NOT NULL DEFAULT now(),
    CONSTRAINT fk_payment_methods_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE INDEX idx_payment_methods_tenant ON payment_methods (tenant_id);

CREATE TABLE payments (
    id                uuid           PRIMARY KEY NOT NULL,
    tenant_id         uuid           NOT NULL,
    service_order_id  uuid           NOT NULL,
    payment_method_id uuid,
    method_name       varchar(80)    NOT NULL,
    amount            numeric(10, 2) NOT NULL,
    paid_at           timestamp      NOT NULL DEFAULT now(),
    created_at        timestamp      NOT NULL DEFAULT now(),
    updated_at        timestamp      NOT NULL DEFAULT now(),
    CONSTRAINT fk_payments_tenant FOREIGN KEY (tenant_id)         REFERENCES tenants         (id),
    CONSTRAINT fk_payments_so     FOREIGN KEY (service_order_id)  REFERENCES service_orders  (id),
    CONSTRAINT fk_payments_pm     FOREIGN KEY (payment_method_id) REFERENCES payment_methods (id)
);

CREATE INDEX idx_payments_so     ON payments (service_order_id);
CREATE INDEX idx_payments_tenant ON payments (tenant_id);

COMMENT ON COLUMN payments.method_name IS 'snapshot do nome da forma no momento do pagamento';

-- Semeia as formas de pagamento padrao para os tenants ja existentes.
INSERT INTO payment_methods (id, tenant_id, name)
SELECT gen_random_uuid(), t.id, m.name
FROM tenants t
CROSS JOIN (VALUES ('Dinheiro'), ('PIX'), ('Cartao de credito'), ('Cartao de debito')) AS m(name);
