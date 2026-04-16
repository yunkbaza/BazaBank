package br.com.banco.transferencia.application.usecases;

import br.com.banco.transferencia.application.ports.out.*;
import br.com.banco.transferencia.domain.*;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

public class RealizarTransferenciaUseCase {
    private final ContaRepositoryPort contaRepository;
    private final TransacaoRepositoryPort transacaoRepository;
    private final TransferenciaEventPublisherPort eventPublisher;

    public RealizarTransferenciaUseCase(ContaRepositoryPort c, TransacaoRepositoryPort t, TransferenciaEventPublisherPort e) {
        this.contaRepository = c;
        this.transacaoRepository = t;
        this.eventPublisher = e;
    }

    @Transactional
    public Transacao executar(UUID origemId, UUID destinoId, BigDecimal valor) {
        if (origemId.equals(destinoId)) throw new IllegalArgumentException("Contas iguais");

        // - Ordenação de UUIDs para evitar Deadlock em transferências simultâneas
        Conta origem, destino;
        if (origemId.compareTo(destinoId) < 0) {
            origem = buscarComLock(origemId);
            destino = buscarComLock(destinoId);
        } else {
            destino = buscarComLock(destinoId);
            origem = buscarComLock(origemId);
        }

        origem.debitar(valor);
        destino.creditar(valor);

        Transacao transacao = new Transacao(UUID.randomUUID(), origemId, destinoId, valor);
        transacao.completar();

        contaRepository.salvar(origem);
        contaRepository.salvar(destino);
        transacaoRepository.salvar(transacao);

        return transacao;
    }

    private Conta buscarComLock(UUID id) {
        return contaRepository.buscarPorIdComLock(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + id));
    }
}