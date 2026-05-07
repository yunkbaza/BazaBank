# ==========================================
# ETAPA 1: CONSTRUÇÃO (BUILD)
# ==========================================
# Usamos a imagem do JDK completo (com ferramentas de compilação)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copiamos os ficheiros de configuração do Maven e o seu código fonte
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Damos permissão ao Maven e compilamos o projeto (ignorando testes para acelerar o deploy)
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# ==========================================
# ETAPA 2: EXECUÇÃO (RUN)
# ==========================================
# Usamos a imagem do JRE (mais leve, feita apenas para rodar a aplicação)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos o ficheiro .jar gerado lá em cima na Etapa 1
COPY --from=build /app/target/*.jar app.jar

# Expomos a porta da aplicação (o Render usará a porta que configurámos no application.properties)
EXPOSE 8081

# O comando que o Docker vai rodar para ligar o banco
ENTRYPOINT ["java", "-jar", "app.jar"]