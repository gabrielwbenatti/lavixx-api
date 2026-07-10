-- Convite de usuario: token de uso unico para o funcionario definir a senha.
-- Fica preenchido enquanto o convite esta pendente; e limpo ao aceitar (definir senha).
ALTER TABLE users ADD COLUMN invite_token varchar(64);

-- Unicidade apenas quando ha token (varios NULL sao permitidos).
CREATE UNIQUE INDEX idx_users_invite_token ON users (invite_token) WHERE invite_token IS NOT NULL;
