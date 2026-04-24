CREATE TABLE pagamento (
    id SERIAL PRIMARY KEY,
    valor DECIMAL(14, 2) NOT NULL,
    comprador_id INT NOT NULL,
    vendedor_id INT,
    tipo VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    correlacao_id UUID NOT NULL UNIQUE
);

CREATE TABLE saldo (
    usuario_id INT PRIMARY KEY,
    valor DECIMAL(14, 2) NOT NULL
);

CREATE TABLE transacao (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL,
    tipo VARCHAR(32) NOT NULL,
    valor DECIMAL(14, 2) NOT NULL,
    data TIMESTAMP NOT NULL,
    referencia_externa_id UUID NOT NULL
);

CREATE INDEX idx_transacao_usuario ON transacao (usuario_id);
