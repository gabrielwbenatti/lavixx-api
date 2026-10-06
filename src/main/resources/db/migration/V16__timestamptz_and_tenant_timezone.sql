-- Fuso horario: todas as colunas de data/hora passam a `timestamptz` (um instante absoluto),
-- independente do fuso do servidor/sessao. Antes eram `timestamp` (sem fuso) e guardavam a
-- hora local do servidor que gravou o dado, o que quebra ao mover o app para a nuvem (UTC).
--
-- Os dados existentes foram gravados por servidores no Brasil, entao sao interpretados como
-- America/Sao_Paulo (sem horario de verao desde 2019, offset fixo -03).
DO $$
DECLARE
    col record;
BEGIN
    FOR col IN
        SELECT c.table_name, c.column_name
          FROM information_schema.columns c
          JOIN information_schema.tables t
            ON t.table_schema = c.table_schema AND t.table_name = c.table_name
         WHERE c.table_schema = current_schema()
           AND t.table_type = 'BASE TABLE'
           AND c.data_type = 'timestamp without time zone'
           AND c.table_name <> 'flyway_schema_history'
    LOOP
        EXECUTE format(
            'ALTER TABLE %I ALTER COLUMN %I TYPE timestamptz USING %I AT TIME ZONE %L',
            col.table_name, col.column_name, col.column_name, 'America/Sao_Paulo');
    END LOOP;
END $$;

-- Fuso do estabelecimento: usado para fechar o "dia" (relatorios, filtros por data).
ALTER TABLE tenants ADD COLUMN timezone varchar(50) NOT NULL DEFAULT 'America/Sao_Paulo';
