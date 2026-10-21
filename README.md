# 🔐 JWT Auth API

[![Java CI/CD with Maven](https://github.com/icarocavalari/JWT-Auth-API/actions/workflows/ci.yml/badge.svg)](https://github.com/icarocavalari/JWT-Auth-API/actions/workflows/ci.yml)

API REST de autenticação stateless com JWT e autorização por roles, construída com Spring Security.

## 🎯 Sobre o projeto

O usuário se cadastra, faz login, recebe um token JWT e o usa para acessar as rotas protegidas de acordo com o seu papel. Construí o projeto para entender na prática como esse fluxo funciona no Spring Security e para praticar testes em duas camadas.

- Cadastro e login com senha codificada e token validado a cada requisição por um filtro próprio.
- Três roles: `USER` (padrão no cadastro), `ADMIN` e `SUPER_ADMIN`. O super admin é criado automaticamente na inicialização e é o único que pode promover usuários a admin.
- Listagem de usuários com paginação e ordenação, com limite máximo de itens por página.
- Respostas de erro padronizadas, sem expor detalhes internos.
- Documentação interativa com Swagger UI, com suporte a autenticação via token.
- Aplicação inteira containerizada, com pipeline de CI/CD: os testes rodam a cada push e, se passarem, a imagem Docker é publicada automaticamente no GitHub Container Registry.

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
- SpringDoc OpenAPI (Swagger UI)

**Testes**
- JUnit 5
- Mockito
- Testcontainers

**Infraestrutura & DevOps**
- Docker (build multi-stage)
- Docker Compose
- GitHub Actions (CI + publicação da imagem no GHCR)

## 🔌 Endpoints da API

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| `POST` | `/auth/signup` | Público | Cadastra um usuário com a role `USER` |
| `POST` | `/auth/login` | Público | Retorna o token JWT |
| `GET` | `/users/me` | Autenticado | Dados do usuário logado |
| `GET` | `/users` | `ADMIN`, `SUPER_ADMIN` | Lista os usuários com paginação e ordenação |
| `PUT` | `/users/{id}/admin` | `SUPER_ADMIN` | Promove um usuário a admin |

Nas rotas protegidas, o token vai no header `Authorization: Bearer <token>`.

### Paginação e ordenação

O `GET /users` aceita três query params, todos opcionais:

| Parâmetro | Padrão | Descrição |
|---|---|---|
| `page` | `0` | Número da página, começando em 0 |
| `size` | `10` | Itens por página (máximo de 50) |
| `sort` | `id,asc` | Campo e direção da ordenação |

```
GET /users?page=0&size=20&sort=fullName,asc
```

- Um `size` acima de 50 é reduzido para 50, e o valor aplicado volta em `page.size` na resposta.
- Ordenar por um campo que não existe retorna `400 Bad Request`.

A resposta traz os usuários em `content` e os metadados em `page` (`size`, `number`, `totalElements` e `totalPages`).

## 🚀 Como rodar localmente

### Pré-requisitos

- Docker

### Passos

```bash
git clone https://github.com/icarocavalari/JWT-Auth-API.git
cd JWT-Auth-API
docker compose up --build
```

A API fica disponível em `http://localhost:8005`, e a documentação interativa, em `http://localhost:8005/swagger-ui.html`.

### Usuário de demonstração

O super admin é criado automaticamente na primeira inicialização:

| Email | Senha |
|---|---|
| `superadmin@authapi.local` | `SuperAdmin@123` |

Para testar as roles pelo Swagger: cadastre um usuário em `/auth/signup`, faça login com o super admin, copie o token e use o botão **Authorize**. Com ele, é possível listar os usuários e promover o novo usuário a admin.

> Todos os valores de configuração (credenciais do banco, chave do JWT e do super admin) têm padrões de demonstração definidos no `compose.yaml`, apenas para facilitar a execução local. Para sobrescrevê-los, copie o `.env.example` para `.env` e preencha os valores. Em qualquer ambiente real, eles devem ser definidos por variáveis de ambiente.

## 🧪 Testes automatizados

Os testes cobrem duas camadas:

- **Unitários**, com JUnit e Mockito: token expirado, adulterado ou assinado com outra chave, cadastro com senha codificada e role padrão, email duplicado, falhas de login e a conversão da listagem paginada para DTOs.
- **Integração**, com MockMvc e Testcontainers: os endpoints passam pela cadeia de filtros real do Spring Security, conferindo os status HTTP de cada cenário e garantindo que a senha nunca aparece nas respostas. Na listagem, cobrem o tamanho da página, a ordenação, o limite máximo de itens e o erro para campo de ordenação inválido.

Os testes de integração sobem um MySQL descartável, então basta ter o Docker rodando:

```bash
./mvnw verify
```

No GitHub Actions, eles rodam a cada push e Pull Request na `main`. Nos pushes, se todos passarem, a imagem da aplicação é construída e publicada no GHCR com duas tags: `latest` e o hash do commit.

## 💡 Aprendizados

- **Testes de integração pegam o que os unitários não pegam:** foram eles que revelaram que requisições sem token respondiam 403 em vez de 401, e que um token malformado derrubava a API com erro 500.
- **O ambiente de teste também pode esconder bugs:** duas vezes a aplicação lançou `LazyInitializationException` (no login e na promoção a admin) enquanto todos os testes passavam, porque o `@Transactional` dos testes mantinha a sessão do Hibernate aberta. No login, resolvi carregando as roles junto com o usuário (`@EntityGraph`); na promoção, tornando o método do service transacional.
- **Testes não devem depender do ambiente:** com Testcontainers e perfis de configuração separados, eles rodam iguais na minha máquina e no CI, sem nenhum segredo real.
- **Uma imagem Docker deve carregar só o que precisa para rodar:** o build em dois estágios usa JDK e Maven só para gerar o `.jar`, e a imagem final leva apenas o JRE.