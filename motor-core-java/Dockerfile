# Estágio 1: Build (Usa o Maven para compilar o código)
FROM maven:3.9.6-eclipse-temurin-22 AS build
WORKDIR /app
# Copia o pom.xml e baixa as dependências
COPY pom.xml .
RUN mvn dependency:go-offline
# Copia o código fonte e gera o arquivo .jar (ignorando testes por enquanto)
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Run (Imagem super leve só com o Java para rodar o app)
FROM eclipse-temurin:22-jre-alpine
WORKDIR /app
# Pega o .jar que foi gerado no Estágio 1 e joga para cá
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]