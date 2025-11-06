# PetPluss - Cadastro (exemplo)

Este projeto é um exemplo mínimo funcional que implementa uma aplicação Java (Spring Boot) para cadastro de clientes, animais, usuários e consultas, com integração a PostgreSQL (relacional) e MongoDB (NoSQL) para prontuários.

Conteúdo criado automaticamente:
- `src/main/java/com/example/cadastro` - código fonte (entidades, repositórios, controladores, segurança)
- `src/main/resources/db/schema.sql` - script SQL com DDL completo (tabelas, sequências, funções, triggers, views, procedures, índices)
- `src/main/resources/application.properties` - propriedades com placeholders para PostgreSQL e MongoDB

Decisões arquiteturais e justificativas (resumo):

- Banco relacional: PostgreSQL. Usado para dados estruturados (usuarios, grupos, clientes, animais, consultas). PostgreSQL oferece sequências, funções PL/pgSQL, triggers e views.
- Banco NoSQL: MongoDB. Escolhido para armazenar `prontuarios` (histórico clínico), que é semi-estruturado e com campos variáveis. MongoDB armazena documentos BSON/JSON sem schema fixo, permitindo flexibilidade.

Geração de IDs:
- Para IDs críticos (usuário e consulta) foram criadas funções `FN_GERAR_ID_USUARIO()` e `FN_GERAR_ID_CONSULTA()` que usam sequences separadas. Isso evita depender de AUTO_INCREMENT quando IDs precisam de formato especial.
- AUTO_INCREMENT (SERIAL) foi usado em `grupos_usuarios`, `clientes` e `animais` por serem dados internos e de baixo volume (justificativa no script).

Índices:
- `idx_usuarios_email` em `usuarios(email)` para acelerar login.
- `idx_animais_id_cliente` para buscar animais por cliente.
- `idx_consultas_data_hora` para consultas por período (agenda).

Triggers:
- `FN_AUDITA_STATUS_CONSULTA` grava alterações de status de consultas na tabela `auditoria_consultas`.
- `FN_IMPEDE_EXCLUSAO_GRUPO` impede exclusão de grupos que ainda têm usuários associados.

Views:
- `VW_AGENDA_DIARIA` – simplifica consultas para agenda (junta consultas, animais, clientes e veterinários).
- `VW_USUARIOS_PERMISSOES` – facilita autenticação/autorização (email, hash_senha, grupo).

Procedures/Functions:
- `SP_CADASTRAR_CLIENTE_COMPLETO` – insere cliente e primeiro animal dentro de uma transação.
- `FN_TOTAL_CONSULTAS_MES` – retorna número de consultas realizadas em um mês/ano.

Controle de acesso (não usar root/postgres):
- No `schema.sql` há instruções para criar a role `usuario_backend_app` com permissões mínimas (CONNECT, CRUD nas tabelas, EXECUTE em funções/procedures e USAGE em sequences). Crie essa role manualmente como superuser antes de configurar a aplicação.

Como rodar (resumo):
1. Instale PostgreSQL e crie o banco `sua_clinica_db`:
   - como superuser: crie o banco e rode o script `src/main/resources/db/schema.sql` (por `psql -f ...`).
   - crie a role `usuario_backend_app` e conceda permissões conforme o script (ver comentários no final do `schema.sql`).
2. Instale e execute MongoDB (p.ex. `mongod` local) e verifique a URL `mongodb://localhost:27017/clinica_prontuarios`.
3. Atualize `src/main/resources/application.properties` com as credenciais corretas do PostgreSQL.
4. Build e run com Maven:
   ```powershell
   cd Cadastro
   mvn spring-boot:run
   ```

Endpoints principais:
- `POST /api/auth/login` { "email","senha" } -> { "token" }
- `POST /api/auth/register` para criar um usuário demo (apenas exemplo)
- `GET /api/clientes` listar clientes
- `POST /api/clientes` criar cliente + animal (transacional)

Observações e próximos passos recomendados:
- Implementar chamadas às funções `FN_GERAR_ID_USUARIO` e `FN_GERAR_ID_CONSULTA` diretamente no banco (ex: via trigger ou procedure) em vez do placeholder usado no registro.
- Implementar um mecanismo robusto de tokens (JWT) para produção e persistência dos tokens (Redis) em vez do armazenamento em memória.
- Implementar scripts de migração (Flyway/Liquibase) para gerenciar schema em ambientes diferentes.
- Criar frontend simples (HTML/JS ou React) que consuma os endpoints; posso gerar um exemplo se desejar.
