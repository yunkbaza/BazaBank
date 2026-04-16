# Usa a imagem oficial e moderna da Eclipse Temurin para Java 25
FROM eclipse-temurin:25-jre

# Define a pasta de trabalho dentro do Linux do Docker
WORKDIR /app

# Pega o .jar que o seu IntelliJ JÁ compilou
COPY target/*.jar app.jar

# Expõe a porta 8080
EXPOSE 8080

# O comando que o Docker vai rodar para ligar o banco
ENTRYPOINT ["java", "-jar", "app.jar"]