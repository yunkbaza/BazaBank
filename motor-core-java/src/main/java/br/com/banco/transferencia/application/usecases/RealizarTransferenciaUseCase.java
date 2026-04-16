package br.com.banco.transferencia.application.usecases;

import br.com.banco.transferencia.application.ports.out.ContaRepositoryPort;
import br.com.banco.transferencia.application.ports.out.TransacaoRepositoryPort;
import br.com.banco.transferencia.application.ports.out.TransferenciaEventPublisherPort;
import br.com.banco.transferencia.domain.Conta;
import br.com.banco.transferencia.domain.Transacao;
import br.com.banco.transferencia.domain.events.TransferenciaRealizadaEvent;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class RealizarTransferenciaUseCase {

    private final ContaRepositoryPort contaRepository;
    private final TransacaoRepositoryPort transacaoRepository;
    private final TransferenciaEventPublisherPort eventPublisher;

    public RealizarTransferenciaUseCase(
            ContaRepositoryPort contaRepository,
            TransacaoRepositoryPort transacaoRepository,
            TransferenciaEventPublisherPort eventPublisher) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Transacao executar(UUID origemId, UUID destinoId, BigDecimal valor) {
        if (origemId.equals(destinoId)) {
            throw new IllegalArgumentException("Não é possível transferir para a mesma conta.");
        }

        // PREVENÇÃO DE DEADLOCK: Ordenação de aquisição de Locks via UUID
        Conta origem, destino;
        if (origemId.compareTo(destinoId) < 0) {
            origem = buscarContaComLock(origemId, "origem");
            destino = buscarContaComLock(destinoId, "destino");
        } else {
            destino = buscarContaComLock(destinoId, "destino");
            origem = buscarContaComLock(origemId, "origem");
        }

        Transacao transacao = new Transacao(UUID.randomUUID(), origemId, destinoId, valor);

        try {
            origem.debitar(valor);
            destino.creditar(valor);
            transacao.completar();
        } catch (Exception e) {
            transacao.falhar();
            transacaoRepository.salvar(transacao);
            throw e; // O Spring fará o Rollback do saldo, mas a transação falha fica salva!
        }

        contaRepository.salvar(origem);
        contaRepository.salvar(destino);
        transacaoRepository.salvar(transacao);

        eventPublisher.publicar(new TransferenciaRealizadaEvent(
                transacao.getId(), origem.getId(), destino.getId(), valor
        ));

        return transacao;
    }

    private Conta buscarContaComLock(UUID contaId, String tipo) {
        return contaRepository.buscarPorIdComLock(contaId)
                .orElseThrow(() -> new IllegalArgumentException("Conta de " + tipo + " não encontrada."));
    }
}