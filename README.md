# JWT Auth API

[![Java CI with Maven](https://github.com/icarocavalari/JWT-Auth-API/actions/workflows/ci.yml/badge.svg)](https://github.com/icarocavalari/JWT-Auth-API/actions/workflows/ci.yml)

API de autenticação com JWT que construí para entender como funciona, na prática, a autenticação stateless com Spring Security. O usuário se cadastra, faz login e recebe um token, que depois é usado para acessar as rotas protegidas.

Além da autenticação em si (o filtro que valida o token a cada requisição e a configuração do Spring Security em volta dele), o projeto foi onde pratiquei testes em duas camadas:

- **Testes unitários** dos services com JUnit e Mockito, cobrindo geração e validação do token (expirado, adulterado, assinado com outra chave), cadastro com senha codificada e email duplicado, e os casos de falha do login.
- **Testes de integração** dos endpoints com MockMvc, passando pela cadeia de filtros real do Spring Security. Eles conferem os status HTTP (401 sem token ou com token inválido, 409 para email duplicado) e garantem que a senha nunca aparece no JSON das respostas.

Escrevendo esses testes, ficaram alguns aprendizados:

- **Testes de integração pegam o que os unitários não pegam:** Foram eles que mostraram que requisições sem token respondiam 403 em vez de 401 e que um token malformado derrubava a API com erro 500.
- **Testes não devem depender do ambiente:** Com Testcontainers, eles rodam iguais na minha máquina e no CI.
- **Configuração muda por ambiente:** Separei as propriedades em perfis (`local` e `test`), com a chave real do JWT só na minha máquina e uma chave falsa para os testes e o CI.

Usei Java 17, Spring Boot 4, Spring Security, Spring Data JPA, MySQL, Docker Compose, a biblioteca JJWT, JUnit 5, Mockito, Testcontainers e GitHub Actions.

## Como rodar

Você vai precisar de Java 17 e Docker.

O banco é um MySQL 8 configurado no `compose.yaml`, na raiz do projeto: basta subir ele com o Docker Compose. Os dados ficam guardados em um volume, então não se perdem quando o container é desligado.

Na pasta `src/main/resources` tem um `application-local.properties.example` com as chaves necessárias: copie esse arquivo, renomeie tirando o `.example` e preencha com os dados do banco (os mesmos definidos no `compose.yaml`) e uma chave secreta própria para o JWT.

Com isso pronto, é só subir a aplicação pelo Maven Wrapper: ./mvnw spring-boot:run

A API roda na porta 8005.

## Testes

Os testes não dependem do banco local nem do `application-local.properties`: os de integração sobem um MySQL próprio e descartável com Testcontainers, então basta ter o Docker rodando. Eles também rodam automaticamente no GitHub Actions a cada push na `main`.
