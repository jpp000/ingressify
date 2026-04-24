INSERT INTO evento (nome, data_hora, local, status, capacidade)
VALUES ('Show Ingressify', TIMESTAMP '2027-06-01 20:00:00', 'Recife, PE', 'ATIVO', 5000);

INSERT INTO tipo_ingresso (evento_id, nome, valor, quantidade_disponivel, quantidade_total)
VALUES ((SELECT id FROM evento WHERE nome = 'Show Ingressify'), 'Pista', 150.00, 1000, 1000);

INSERT INTO saldo (usuario_id, valor)
VALUES (1, 5000.00);

INSERT INTO saldo (usuario_id, valor)
VALUES (2, 1000.00);
