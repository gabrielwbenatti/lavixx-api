-- Cadastro de funcionarios (quem executa os servicos). Nao e conta de login:
-- lavadores normalmente nao acessam o sistema. Espelha `payment_methods`.
CREATE TABLE employees (
    id         uuid         PRIMARY KEY NOT NULL,
    tenant_id  uuid         NOT NULL,
    name       varchar(150) NOT NULL,
    is_active  boolean      NOT NULL DEFAULT true,
    created_at timestamp    NOT NULL DEFAULT now(),
    updated_at timestamp    NOT NULL DEFAULT now(),
    CONSTRAINT fk_employees_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE INDEX idx_employees_tenant ON employees (tenant_id);
