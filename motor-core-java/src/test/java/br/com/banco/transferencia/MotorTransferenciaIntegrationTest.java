package br.com.banco.transferencia;

import br.com.banco.transferencia.application.ports.out.ContaRepositoryPort;
import br.com.banco.transferencia.application.ports.out.TransferenciaEventPublisherPort;
import br.com.banco.transferencia.application.usecases.RealizarTransferenciaUseCase;
import br.com.banco.transferencia.domain.Conta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class MotorTransferenciaIntegrationTest {

    // 1. Mantemos APENAS o PostgreSQL (Nosso foco é testar o Lock do banco!)
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("banco_db")
            .withUsername("root")
            .withPassword("123");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private RealizarTransferenciaUseCase realizarTransferenciaUseCase;

    @Autowired
    private ContaRepositoryPort contaRepositoryPort;

    // 2. A MÁGICA: Mockamos o Kafka!
    // O Spring vai injetar um "fantasma" do publicador. Ele finge que publica, mas não faz nada.
    // Assim isolamos o teste e focamos apenas na regra de concorrência do PostgreSQL.
    @MockitoBean
    private TransferenciaEventPublisherPort eventPublisher;

    private UUID origemId;
    private UUID destinoId;

    @BeforeEach
    void setup() {
        origemId = UUID.randomUUID();
        destinoId = UUID.randomUUID();

        // O usuário nasce com R$ 5.000
        contaRepositoryPort.salvar(new Conta(origemId, "111", new BigDecimal("5000.00")));
        contaRepositoryPort.salvar(new Conta(destinoId, "222", BigDecimal.ZERO));
    }

    @Test
    void deveEvitarSaldoNegativoQuandoDuasTransferenciasOcorremAoMesmoTempo() throws InterruptedException {
        BigDecimal valorTransferencia = new BigDecimal("5000.00");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        // O usuário tenta hackear o sistema disparando 2 PIX de R$ 5000 ao mesmo tempo

        executor.submit(() -> {
            try {
                realizarTransferenciaUseCase.executar(origemId, destinoId, valorTransferencia);
                System.out.println("Thread 1: Passou a primeira transação!");
            } catch (Exception e) {
                System.out.println("Thread 1 falhou com sucesso (Lock bateu): " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                realizarTransferenciaUseCase.executar(origemId, destinoId, valorTransferencia);
                System.out.println("Thread 2: Passou a segunda transação!");
            } catch (Exception e) {
                System.out.println("Thread 2 falhou com sucesso (Lock bateu): " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executor.shutdown();

        Conta origemAtualizada = contaRepositoryPort.buscarPorId(origemId).get();
        Conta destinoAtualizada = contaRepositoryPort.buscarPorId(destinoId).get();

        // VALIDAÇÃO: Se o banco tem Lock, só uma passa (saldo fica 0). Se não tem, as duas passam (saldo fica -5000).
        assertEquals(new BigDecimal("0.00"), origemAtualizada.getSaldo());
        assertEquals(new BigDecimal("5000.00"), destinoAtualizada.getSaldo());
    }
}