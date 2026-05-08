# ==========================================
# ETAPA 1: CONSTRUÇÃO (BUILD)
# ==========================================
# Atualizado para JDK 21 para suportar o seu código fonte
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiamos os ficheiros essenciais do Maven e o código fonte
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Garante permissão e compila o .jar (ignora testes para ser mais rápido)
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# ==========================================
# ETAPA 2: EXECUÇÃO (RUN)
# ==========================================
# Atualizado para JRE 21 para rodar o servidor
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o .jar fresquinho gerado na etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Define a porta do BazaBank
EXPOSE 8081

# Inicia a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]