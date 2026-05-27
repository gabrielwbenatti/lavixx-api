CREATE TYPE user_role AS ENUM (
    'admin',
    'staff'
);

CREATE TYPE vehicle_type AS ENUM (
    'car',
    'motorcycle',
    'boat',
    'bicycle',
    'other'
);

CREATE TYPE service_status AS ENUM (
    'waiting',
    'in_progress',
    'done',
    'cancelled'
);

CREATE TABLE tenants (
    id         uuid         PRIMARY KEY NOT NULL,
    name       varchar(150) NOT NULL,
    document   varchar(14)  NOT NULL,
    created_at timestamp    NOT NULL DEFAULT now(),
    updated_at timestamp    NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id         uuid         PRIMARY KEY NOT NULL,
    tenant_id  uuid         NOT NULL,
    name       varchar(150) NOT NULL,
    email      varchar(150) NOT NULL,
    password   varchar(255),
    is_active  boolean      NOT NULL DEFAULT false,
    role       user_role    NOT NULL DEFAULT 'staff',
    created_at timestamp    NOT NULL DEFAULT now(),
    updated_at timestamp    NOT NULL DEFAULT now(),
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE TABLE customers (
    id         uuid         PRIMARY KEY NOT NULL,
    tenant_id  uuid         NOT NULL,
    name       varchar(150) NOT NULL,
    document   varchar(14),
    created_at timestamp    NOT NULL DEFAULT now(),
    updated_at timestamp    NOT NULL DEFAULT now(),
    CONSTRAINT fk_customers_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE TABLE vehicles (
    id           uuid          PRIMARY KEY NOT NULL,
    tenant_id    uuid          NOT NULL,
    customer_id  uuid          NOT NULL,
    type         vehicle_type  NOT NULL DEFAULT 'car',
    plate        varchar(10),
    identifier   varchar(60),
    nickname     varchar(80),
    manufacturer varchar(60),
    model        varchar(60),
    color        varchar(30),
    year         smallint,
    created_at   timestamp     NOT NULL DEFAULT now(),
    updated_at   timestamp     NOT NULL DEFAULT now(),
    CONSTRAINT fk_vehicles_tenant   FOREIGN KEY (tenant_id)   REFERENCES tenants   (id),
    CONSTRAINT fk_vehicles_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE services (
    id         uuid           PRIMARY KEY NOT NULL,
    tenant_id  uuid           NOT NULL,
    name       varchar(150)   NOT NULL,
    price      numeric(10, 2) NOT NULL,
    created_at timestamp      NOT NULL DEFAULT now(),
    updated_at timestamp      NOT NULL DEFAULT now(),
    CONSTRAINT fk_services_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE TABLE service_orders (
    id          uuid           PRIMARY KEY NOT NULL,
    tenant_id   uuid           NOT NULL,
    customer_id uuid           NOT NULL,
    vehicle_id  uuid           NOT NULL,
    service_id  uuid           NOT NULL,
    status      service_status NOT NULL DEFAULT 'waiting',
    price       numeric(10, 2) NOT NULL DEFAULT 0,
    created_at  timestamp      NOT NULL DEFAULT now(),
    updated_at  timestamp      NOT NULL DEFAULT now(),
    finished_at timestamp,
    CONSTRAINT fk_service_orders_tenant   FOREIGN KEY (tenant_id)   REFERENCES tenants   (id),
    CONSTRAINT fk_service_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_service_orders_vehicle  FOREIGN KEY (vehicle_id)  REFERENCES vehicles  (id),
    CONSTRAINT fk_service_orders_service  FOREIGN KEY (service_id)  REFERENCES services  (id)
);

CREATE UNIQUE INDEX idx_tenants_document        ON tenants        (document);
CREATE        INDEX idx_users_tenant            ON users          (tenant_id);
CREATE UNIQUE INDEX idx_users_tenant_email      ON users          (tenant_id, email);
CREATE        INDEX idx_customers_tenant        ON customers      (tenant_id);
CREATE        INDEX idx_customers_tenant_doc    ON customers      (tenant_id, document);
CREATE        INDEX idx_vehicles_tenant         ON vehicles       (tenant_id);
CREATE        INDEX idx_vehicles_tenant_cust    ON vehicles       (tenant_id, customer_id);
CREATE        INDEX idx_vehicles_tenant_plate   ON vehicles       (tenant_id, plate);
CREATE        INDEX idx_services_tenant         ON services       (tenant_id);
CREATE        INDEX idx_service_orders_tenant   ON service_orders (tenant_id);
CREATE        INDEX idx_service_orders_t_cust   ON service_orders (tenant_id, customer_id);
CREATE        INDEX idx_service_orders_t_vehi   ON service_orders (tenant_id, vehicle_id);
CREATE        INDEX idx_service_orders_t_stat   ON service_orders (tenant_id, status);

COMMENT ON COLUMN tenants.document   IS 'cpf/cnpj';
COMMENT ON COLUMN customers.document IS 'cpf/cnpj';
COMMENT ON COLUMN users.is_active    IS 'false ate o usuario definir a senha via convite';
