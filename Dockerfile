FROM eclipse-temurin:17-jdk-noble AS build

WORKDIR /app

COPY .mvn ./.mvn
COPY pom.xml .
COPY mvnw .

# Baixando todas as dependecias do projeto
RUN ./mvnw dependency:go-offline

COPY src/ /app/src

# Compilando e gerando o .jar
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre-noble

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8005

ENTRYPOINT ["java", "-jar", "app.jar"]