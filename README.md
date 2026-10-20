# 🔐 JWT Auth API

[![Java CI with Maven](https://github.com/icarocavalari/JWT-Auth-API/actions/workflows/ci.yml/badge.svg)](https://github.com/icarocavalari/JWT-Auth-API/actions/workflows/ci.yml)

API REST de autenticação stateless com JWT e autorização por roles, construída com Spring Security.

## 🎯 Sobre o projeto

O usuário se cadastra, faz login, recebe um token JWT e o usa para acessar as rotas protegidas de acordo com o seu papel. Construí o projeto para entender na prática como esse fluxo funciona no Spring Security e para praticar testes em duas camadas.

- Cadastro e login com senha codificada e token validado a cada requisição por um filtro próprio.
- Três roles: `USER` (padrão no cadastro), `ADMIN` e `SUPER_ADMIN`. O super admin é criado automaticamente na inicialização e é o único que pode promover usuários a admin.
- Respostas de erro padronizadas, sem expor detalhes internos: 401, 403, 404 e 409.
- Documentação interativa com Swagger UI, com suporte a autenticação via token.
- Aplicação inteira containerizada, com testes rodando automaticamente no CI.

## 🛠 Stack tecnológica

**Backend**
- Java 17
- Spring Boot 4
- Spring Security
- Spring Data JPA
- JJWT

**Banco de dados**
- MySQL 8

**Documentação de API**
- springdoc-openapi (Swagger UI)

**Testes**
- JUnit 5
- Mockito
- MockMvc
- Testcontainers

**Infraestrutura & DevOps**
- Docker (build multi-stage)
- Docker Compose
- GitHub Actions

## 🔌 Endpoints da API

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/auth/signup` | Público | Cadastra um usuário com a role `USER` |
| `POST` | `/auth/login` | Público | Retorna o token JWT |
| `GET` | `/users/me` | Autenticado | Dados do usuário logado |
| `GET` | `/users` | `ADMIN`, `SUPER_ADMIN` | Lista todos os usuários |
| `PUT` | `/users/{id}/admin` | `SUPER_ADMIN` | Promove um usuário a admin |

Nas rotas protegidas, o token vai no header `Authorization: Bearer <token>`.

## 🚀 Como rodar localmente

### Pré-requisitos

- Docker e Docker Compose

### Passos

```bash
git clone https://github.com/icarocavalari/JWT-Auth-API.git
cd JWT-Auth-API
cp .env.example .env
```

Preencha o `.env` com os valores. A chave do JWT pode ser gerada com `openssl rand -base64 32`. Depois:

```bash
docker compose up --build
```

A API fica disponível em `http://localhost:8005`.

A documentação interativa fica em `http://localhost:8005/swagger-ui.html`. Para testar as rotas protegidas, faça login, copie o token e use o botão **Authorize**.

## 🧪 Testes automatizados

Os testes cobrem duas camadas:

- **Unitários**, com JUnit e Mockito: token expirado, adulterado ou assinado com outra chave, cadastro com senha codificada e role padrão, email duplicado e falhas de login.
- **Integração**, com MockMvc e Testcontainers: os endpoints passam pela cadeia de filtros real do Spring Security, conferindo os status HTTP de cada cenário e garantindo que a senha nunca aparece nas respostas.

Os testes de integração sobem um MySQL descartável, então basta ter o Docker rodando:

```bash
./mvnw verify
```

Eles também rodam automaticamente no GitHub Actions a cada push na `main`.

## 💡 Aprendizados

- **Testes de integração pegam o que os unitários não pegam:** foram eles que revelaram que requisições sem token respondiam 403 em vez de 401, e que um token malformado derrubava a API com erro 500.
- **O ambiente de teste também pode esconder bugs:** duas vezes a aplicação lançou `LazyInitializationException` (no login e na promoção a admin) enquanto todos os testes passavam, porque o `@Transactional` dos testes mantinha a sessão do Hibernate aberta. No login, resolvi carregando as roles junto com o usuário (`@EntityGraph`); na promoção, tornando o método do service transacional.
- **Testes não devem depender do ambiente:** com Testcontainers e perfis de configuração separados, eles rodam iguais na minha máquina e no CI, sem nenhum segredo real.
- **Uma imagem Docker deve carregar só o que precisa para rodar:** o build em dois estágios usa JDK e Maven só para gerar o `.jar`, e a imagem final leva apenas o JRE.