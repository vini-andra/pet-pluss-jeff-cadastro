-- PostgreSQL schema for PetPluss clinic
-- Tables, sequences, functions, triggers, views, indexes, procedures and roles

-- 1. Sequences for ID generation (used by functions for critical IDs)
CREATE SEQUENCE IF NOT EXISTS seq_usuario_id;
CREATE SEQUENCE IF NOT EXISTS seq_consulta_id;

-- 2. grupos_usuarios
CREATE TABLE IF NOT EXISTS grupos_usuarios (
    id_grupo SERIAL PRIMARY KEY,
    nome_grupo VARCHAR(50) UNIQUE NOT NULL
);

-- 3. usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario VARCHAR(50) PRIMARY KEY,
    id_grupo INT REFERENCES grupos_usuarios(id_grupo),
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    hash_senha VARCHAR(255) NOT NULL,
    ativo BOOLEAN DEFAULT true
);

-- 4. clientes
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) UNIQUE,
    telefone VARCHAR(15)
);

-- 5. animais
CREATE TABLE IF NOT EXISTS animais (
    id_animal SERIAL PRIMARY KEY,
    id_cliente INT REFERENCES clientes(id_cliente),
    nome VARCHAR(50),
    especie VARCHAR(30),
    raca VARCHAR(30),
    data_nascimento DATE
);

-- 6. consultas
CREATE TABLE IF NOT EXISTS consultas (
    id_consulta VARCHAR(50) PRIMARY KEY,
    id_animal INT REFERENCES animais(id_animal),
    id_veterinario VARCHAR(50) REFERENCES usuarios(id_usuario),
    data_hora TIMESTAMP,
    status VARCHAR(20)
);

-- 7. auditoria_consultas (trigger log)
CREATE TABLE IF NOT EXISTS auditoria_consultas (
    id_auditoria SERIAL PRIMARY KEY,
    id_consulta_afetada VARCHAR(50),
    status_antigo VARCHAR(20),
    status_novo VARCHAR(20),
    usuario_modificador VARCHAR(100),
    data_modificacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Functions for ID generation (critical IDs)
CREATE OR REPLACE FUNCTION FN_GERAR_ID_USUARIO()
RETURNS VARCHAR AS $$
DECLARE
    novo_id VARCHAR(50);
BEGIN
    novo_id := 'USR_' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '_' || LPAD(NEXTVAL('seq_usuario_id')::TEXT, 3, '0');
    RETURN novo_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION FN_GERAR_ID_CONSULTA()
RETURNS VARCHAR AS $$
BEGIN
    RETURN 'CON_' || TO_CHAR(CURRENT_DATE, 'YYYY') || '_' || LPAD(NEXTVAL('seq_consulta_id')::TEXT, 5, '0');
END;
$$ LANGUAGE plpgsql;

-- 9. Indexes (justificativa in README)
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_animais_id_cliente ON animais(id_cliente);
CREATE INDEX IF NOT EXISTS idx_consultas_data_hora ON consultas(data_hora);

-- 10. Triggers
-- Audit status change
CREATE OR REPLACE FUNCTION FN_AUDITA_STATUS_CONSULTA()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO auditoria_consultas (id_consulta_afetada, status_antigo, status_novo, usuario_modificador)
        VALUES (OLD.id_consulta, OLD.status, NEW.status, current_user);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS TRG_AUDITA_CONSULTA ON consultas;
CREATE TRIGGER TRG_AUDITA_CONSULTA
AFTER UPDATE ON consultas
FOR EACH ROW
EXECUTE FUNCTION FN_AUDITA_STATUS_CONSULTA();

-- Prevent delete of group in use
CREATE OR REPLACE FUNCTION FN_IMPEDE_EXCLUSAO_GRUPO()
RETURNS TRIGGER AS $$
DECLARE
    contagem_usuarios INT;
BEGIN
    SELECT COUNT(*) INTO contagem_usuarios FROM usuarios WHERE id_grupo = OLD.id_grupo;
    IF contagem_usuarios > 0 THEN
        RAISE EXCEPTION 'Impossível excluir grupo. Existem % usuários associados.', contagem_usuarios;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS TRG_IMPEDE_EXCLUSAO_GRUPO ON grupos_usuarios;
CREATE TRIGGER TRG_IMPEDE_EXCLUSAO_GRUPO
BEFORE DELETE ON grupos_usuarios
FOR EACH ROW
EXECUTE FUNCTION FN_IMPEDE_EXCLUSAO_GRUPO();

-- 11. Views
CREATE OR REPLACE VIEW VW_AGENDA_DIARIA AS
SELECT 
    c.id_consulta,
    c.data_hora,
    c.status,
    a.nome AS nome_animal,
    a.especie,
    cl.nome AS nome_cliente,
    cl.telefone AS telefone_cliente,
    u.nome AS nome_veterinario
FROM consultas c
JOIN animais a ON c.id_animal = a.id_animal
JOIN clientes cl ON a.id_cliente = cl.id_cliente
LEFT JOIN usuarios u ON c.id_veterinario = u.id_usuario;

CREATE OR REPLACE VIEW VW_USUARIOS_PERMISSOES AS
SELECT 
    u.id_usuario,
    u.email,
    u.hash_senha,
    u.ativo,
    g.nome_grupo
FROM usuarios u
LEFT JOIN grupos_usuarios g ON u.id_grupo = g.id_grupo;

-- 12. Procedures and functions
CREATE OR REPLACE PROCEDURE SP_CADASTRAR_CLIENTE_COMPLETO(
    p_nome_cliente VARCHAR,
    p_cpf VARCHAR,
    p_telefone VARCHAR,
    p_nome_animal VARCHAR,
    p_especie VARCHAR,
    p_raca VARCHAR
)
LANGUAGE plpgsql AS $$
DECLARE
    v_id_cliente INT;
BEGIN
    INSERT INTO clientes (nome, cpf, telefone)
    VALUES (p_nome_cliente, p_cpf, p_telefone)
    RETURNING id_cliente INTO v_id_cliente;

    INSERT INTO animais (id_cliente, nome, especie, raca)
    VALUES (v_id_cliente, p_nome_animal, p_especie, p_raca);

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
$$;

CREATE OR REPLACE FUNCTION FN_TOTAL_CONSULTAS_MES(p_mes INT, p_ano INT)
RETURNS INT AS $$
DECLARE
    total_consultas INT;
BEGIN
    SELECT COUNT(*) INTO total_consultas
    FROM consultas
    WHERE EXTRACT(MONTH FROM data_hora) = p_mes
      AND EXTRACT(YEAR FROM data_hora) = p_ano
      AND status = 'Realizada';

    RETURN total_consultas;
END;
$$ LANGUAGE plpgsql;

-- 13. Create application role (do not use postgres/root in application)
-- Please create the role manually and set a secure password before running the application.
-- Example (run as a superuser):
-- CREATE ROLE usuario_backend_app LOGIN PASSWORD 'SuaSenhaSuperSecreta';
-- GRANT CONNECT ON DATABASE sua_clinica_db TO usuario_backend_app;
-- GRANT USAGE ON SCHEMA public TO usuario_backend_app;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON usuarios, grupos_usuarios, clientes, animais, consultas TO usuario_backend_app;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO usuario_backend_app;
-- GRANT EXECUTE ON PROCEDURE SP_CADASTRAR_CLIENTE_COMPLETO TO usuario_backend_app;
-- GRANT EXECUTE ON FUNCTION FN_TOTAL_CONSULTAS_MES, FN_GERAR_ID_USUARIO, FN_GERAR_ID_CONSULTA TO usuario_backend_app;

-- End of schema
