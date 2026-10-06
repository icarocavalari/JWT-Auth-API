# JWT Auth API

API de autenticação com JWT que construí para entender como funciona, na prática, a autenticação stateless com Spring Security. O usuário se cadastra, faz login e recebe um token, que depois é usado para acessar as rotas protegidas.

A parte que mais me ensinou foi o filtro que valida o token a cada requisição e a configuração do Spring Security em volta dele. Também escrevi testes unitários para os services, que foi onde mais pratiquei JUnit e Mockito.

Usei Java 17, Spring Boot 4, Spring Security, Spring Data JPA, MySQL, Docker Compose e a biblioteca JJWT.

Para rodar o projeto, você vai precisar de Java 17 e Docker. O banco é um MySQL 8 configurado no compose.yaml, na raiz do projeto: basta subir ele com o Docker Compose. Os dados ficam guardados em um volume, então não se perdem quando o container é desligado.

As configurações sensíveis ficam num arquivo application-local.properties, que está fora do Git. Na pasta src/main/resources tem um application-local.properties.example com as chaves necessárias: copie esse arquivo, renomeie tirando o .example e preencha com os dados do banco (os mesmos definidos no compose.yaml) e uma chave secreta própria para o JWT (dá para gerar uma com o openssl, em Base64).

Com isso pronto, é só subir a aplicação pelo Maven Wrapper. A API roda na porta 8005, e os testes rodam com o comando test do próprio Maven Wrapper (o banco precisa estar de pé para eles também).

Esta primeira versão ainda não tem roles nem refresh token, e os testes cobrem só a camada de service. São os próximos passos.
