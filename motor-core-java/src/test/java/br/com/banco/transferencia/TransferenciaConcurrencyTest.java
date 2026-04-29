package br.com.banco.transferencia;

import br.com.banco.transferencia.application.usecases.RealizarTransferenciaUseCase;
import br.com.banco.transferencia.infrastructure.persistence.ContaEntity;
import br.com.banco.transferencia.infrastructure.persistence.ContaJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class TransferenciaConcurrencyTest {

    @Autowired
    private RealizarTransferenciaUseCase transferenciaUseCase;

    @Autowired
    private ContaJpaRepository contaJpaRepository;

    private UUID idOrigem;
    private UUID idDestino;

    @BeforeEach
    public void setup() {
        // Limpa o banco antes do teste para não haver sujeira de testes anteriores
        contaJpaRepository.deleteAll();

        // Cria a conta de Origem com saldo inicial de R$ 100,00
        ContaEntity origem = new ContaEntity();
        origem.setId(UUID.randomUUID());
        origem.setSaldo(new BigDecimal("100.00"));
        // Se a sua ContaEntity exigir outros campos obrigatórios (como nome ou cpf), preencha-os aqui:
        // origem.setNome("João");

        origem = contaJpaRepository.save(origem);
        idOrigem = origem.getId();

        // Cria a conta de Destino com saldo inicial de R$ 0,00
        ContaEntity destino = new ContaEntity();
        destino.setId(UUID.randomUUID());
        destino.setSaldo(BigDecimal.ZERO);

        destino = contaJpaRepository.save(destino);
        idDestino = destino.getId();
    }

    @Test
    public void deveImpedirSaldoNegativoEmTransferenciasSimultaneas() throws InterruptedException {
        int totalDeRequisicoesSimultaneas = 10;
        ExecutorService executor = Executors.newFixedThreadPool(totalDeRequisicoesSimultaneas);
        CountDownLatch latch = new CountDownLatch(totalDeRequisicoesSimultaneas);

        AtomicInteger transferenciasComSucesso = new AtomicInteger(0);
        AtomicInteger transferenciasRejeitadas = new AtomicInteger(0);

        // Dispara 10 transferências EXATAMENTE ao mesmo tempo
        for (int i = 0; i < totalDeRequisicoesSimultaneas; i++) {
            executor.execute(() -> {
                try {
                    // Todas as threads tentam transferir os mesmos R$ 100,00
                    transferenciaUseCase.executar(idOrigem, idDestino, new BigDecimal("100.00"));
                    transferenciasComSucesso.incrementAndGet();
                } catch (Exception e) {
                    // Se falhou (deve falhar por falta de saldo), contabiliza aqui
                    transferenciasRejeitadas.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Faz a thread de teste principal aguardar o caos acabar
        latch.await();

        System.out.println("Sucessos: " + transferenciasComSucesso.get());
        System.out.println("Rejeições: " + transferenciasRejeitadas.get());

        // Asserts para garantir matematicamente que o Pessimistic Lock funcionou
        assertEquals(1, transferenciasComSucesso.get(), "Apenas 1 transferência deve ter sucesso");
        assertEquals(9, transferenciasRejeitadas.get(), "As outras 9 devem falhar por falta de saldo");

        // Valida se o banco de dados realmente ficou com o saldo zero, e não negativo
        ContaEntity origemPosTeste = contaJpaRepository.findById(idOrigem).get();
        assertEquals(0, origemPosTeste.getSaldo().compareTo(BigDecimal.ZERO), "O saldo da conta de origem deve ser R$ 0,00");
    }
}