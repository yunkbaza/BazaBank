package br.com.banco.transferencia;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers // Diz ao Spring para ligar os motores do Docker antes de testar
class MotorTransferenciaIntegrationTest {

    // 1. O Java pede ao Docker para criar um Banco de Dados real
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("banco_teste")
            .withUsername("root")
            .withPassword("123");

    // 2. O Java pede ao Docker para criar um Kafka real
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    // 3. O Java injeta as credenciais dos contentores invisíveis diretamente no Spring Boot
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void contextoCarregaComSucessoNoDocker() {
        // Se a execução chegar a esta linha, significa que:
        // - O PostgreSQL subiu no Docker
        // - O Kafka subiu no Docker
        // - O Hibernate criou as tabelas com sucesso
        // - O Tomcat conectou em tudo sem atirar exceções

        System.out.println("🐳 TESTCONTAINERS DE PÉ! O Spring Boot conectou à infraestrutura efémera com sucesso.");
        assertTrue(postgres.isRunning());
        assertTrue(kafka.isRunning());
    }
}