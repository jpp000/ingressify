CREATE TABLE evento (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    local VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    capacidade INT NOT NULL
);

CREATE TABLE tipo_ingresso (
    id SERIAL PRIMARY KEY,
    evento_id INT NOT NULL REFERENCES evento (id),
    nome VARCHAR(255) NOT NULL,
    valor DECIMAL(14, 2) NOT NULL,
    quantidade_disponivel INT NOT NULL,
    quantidade_total INT NOT NULL
);

CREATE INDEX idx_tipo_ingresso_evento ON tipo_ingresso (evento_id);

CREATE TABLE ingresso (
    id UUID PRIMARY KEY,
    tipo_ingresso_id INT NOT NULL REFERENCES tipo_ingresso (id),
    evento_id INT NOT NULL REFERENCES evento (id),
    proprietario_id INT NOT NULL,
    status VARCHAR(32) NOT NULL
);

CREATE INDEX idx_ingresso_proprietario ON ingresso (proprietario_id);
CREATE INDEX idx_ingresso_evento ON ingresso (evento_id);

CREATE TABLE anuncio_revenda (
    id SERIAL PRIMARY KEY,
    ingresso_id UUID NOT NULL REFERENCES ingresso (id),
    vendedor_id INT NOT NULL,
    valor DECIMAL(14, 2) NOT NULL,
    comprador_reservado_id INT,
    status VARCHAR(32) NOT NULL,
    correlacao_pagamento UUID
);

CREATE INDEX idx_anuncio_correlacao ON anuncio_revenda (correlacao_pagamento);
CREATE INDEX idx_anuncio_ingresso ON anuncio_revenda (ingresso_id);

CREATE TABLE compra_pendente (
    id UUID PRIMARY KEY,
    tipo_ingresso_id INT NOT NULL REFERENCES tipo_ingresso (id),
    evento_id INT NOT NULL REFERENCES evento (id),
    quantidade INT NOT NULL,
    comprador_id INT NOT NULL,
    valor_total DECIMAL(14, 2) NOT NULL,
    criada_em TIMESTAMP NOT NULL
);
