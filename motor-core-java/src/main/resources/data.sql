-- Criando duas contas para teste
INSERT INTO contas (id, numero, saldo)
VALUES ('11111111-1111-1111-1111-111111111111', '10001-X', 5000.00)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO contas (id, numero, saldo)
VALUES ('22222222-2222-2222-2222-222222222222', '20002-Y', 0.00)
    ON CONFLICT (id) DO NOTHING;
