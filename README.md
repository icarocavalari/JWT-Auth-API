# JWT Auth API

[![Java CI with Maven](https://github.com/icarocavalari/JWT-Auth-API/actions/workflows/ci.yml/badge.svg)](https://github.com/icarocavalari/JWT-Auth-API/actions/workflows/ci.yml)

API REST de autenticação stateless com JWT e autorização por roles, construída com Spring Security para entender na prática como esse fluxo funciona: o usuário se cadastra, faz login, recebe um token e o usa para acessar as rotas protegidas de acordo com o seu papel.

**Stack:** Java, Spring Boot, Spring Security, Spring Data JPA, MySQL, JJWT, JUnit 5, Mockito, Testcontainers, Docker, Docker Compose e GitHub Actions.

## Funcionalidades

- Cadastro e login com senha codificada e token JWT validado a cada requisição por um filtro próprio.
- Três roles: `USER` (padrão no cadastro), `ADMIN` e `SUPER_ADMIN`. O super admin é criado automaticamente na inicialização e é o único que pode promover usuários a admin.
- Respostas de erro padronizadas, sem expor detalhes internos: 401, 403, 404 e 409.
- **Testes unitários** dos services com JUnit e Mockito: token expirado, adulterado ou assinado com outra chave, cadastro e falhas de login.
- **Testes de integração** dos endpoints com MockMvc e Testcontainers, passando pela cadeia de filtros real do Spring Security.
- CI no GitHub Actions e aplicação inteira containerizada.

## Endpoints

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/auth/signup` | Público | Cadastra um usuário com a role `USER` |
| `POST` | `/auth/login` | Público | Retorna o token JWT |
| `GET` | `/users/me` | Autenticado | Dados do usuário logado |
| `GET` | `/users` | `ADMIN`, `SUPER_ADMIN` | Lista todos os usuários |
| `PUT` | `/users/{id}/admin` | `SUPER_ADMIN` | Promove um usuário a admin |

Nas rotas protegidas, o token vai no header `Authorization: Bearer <token>`.

## Como rodar

Você só precisa do Docker.

1. Copie o `.env.example` para `.env` e preencha os valores. A chave do JWT pode ser gerada com `openssl rand -base64 32`.
2. Suba a aplicação e o banco:

```bash
docker compose up --build
```

A API fica disponível em `http://localhost:8005`.

## Testes

Os testes de integração sobem um MySQL descartável com Testcontainers, então basta ter o Docker rodando:

```bash
./mvnw verify
```

Eles também rodam automaticamente no GitHub Actions a cada push na `main`.

## Aprendizados

- **Testes de integração pegam o que os unitários não pegam:** foram eles que revelaram que requisições sem token respondiam 403 em vez de 401, e que um token malformado derrubava a API com erro 500.
- **O ambiente de teste também pode esconder bugs:** o login lançava `LazyInitializationException` na aplicação real, mas os testes passavam porque o `@Transactional` mantinha a sessão do Hibernate aberta. Resolvi carregando as roles com `@EntityGraph`.
- **Testes não devem depender do ambiente:** com Testcontainers e perfis de configuração separados, eles rodam iguais na minha máquina e no CI, sem nenhum segredo real.
- **Uma imagem Docker deve carregar só o que precisa para rodar:** o Dockerfile usa build em dois estágios, com JDK e Maven só para gerar o `.jar` e apenas o JRE na imagem final.