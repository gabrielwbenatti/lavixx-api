-- Despesas avulsas do estabelecimento, usadas no fechamento (lucro = recebido - despesas).
CREATE TYPE expense_category AS ENUM (
    'cleaning_supplies',
    'water',
    'electricity',
    'rent',
    'salaries',
    'maintenance',
    'taxes',
    'other'
);

CREATE TABLE expenses (
    id           uuid             PRIMARY KEY NOT NULL,
    tenant_id    uuid             NOT NULL,
    category     expense_category NOT NULL DEFAULT 'other',
    amount       numeric(10, 2)   NOT NULL,
    expense_date date             NOT NULL,
    description  varchar(255),
    created_at   timestamp        NOT NULL DEFAULT now(),
    updated_at   timestamp        NOT NULL DEFAULT now(),
    CONSTRAINT fk_expenses_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE INDEX idx_expenses_tenant ON expenses (tenant_id);
CREATE INDEX idx_expenses_tenant_date ON expenses (tenant_id, expense_date);
