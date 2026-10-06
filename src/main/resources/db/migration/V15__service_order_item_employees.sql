-- Funcionarios que executaram cada item da OS (um ou mais por item).
CREATE TABLE service_order_item_employees (
    service_order_item_id uuid NOT NULL,
    employee_id           uuid NOT NULL,
    PRIMARY KEY (service_order_item_id, employee_id),
    CONSTRAINT fk_soie_item     FOREIGN KEY (service_order_item_id) REFERENCES service_order_items (id) ON DELETE CASCADE,
    CONSTRAINT fk_soie_employee FOREIGN KEY (employee_id)           REFERENCES employees (id)
);

CREATE INDEX idx_soie_employee ON service_order_item_employees (employee_id);
