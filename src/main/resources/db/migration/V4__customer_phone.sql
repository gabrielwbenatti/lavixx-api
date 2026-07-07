ALTER TABLE customers ADD COLUMN phone varchar(20);

COMMENT ON COLUMN customers.phone IS 'telefone/celular apenas com digitos (DDD + numero)';
