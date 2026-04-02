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

        Conta origem = contaRepository.buscarPorIdComLock(origemId)
                .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada."));

        Conta destino = contaRepository.buscarPorIdComLock(destinoId)
                .orElseThrow(() -> new IllegalArgumentException("Conta de destino não encontrada."));

        Transacao transacao = new Transacao(UUID.randomUUID(), origemId, destinoId, valor);

        try {
            origem.debitar(valor);
            destino.creditar(valor);
            transacao.completar();
        } catch (Exception e) {
            transacao.falhar();
            transacaoRepository.salvar(transacao);
            throw e;
        }

        contaRepository.salvar(origem);
        contaRepository.salvar(destino);
        transacaoRepository.salvar(transacao);

        eventPublisher.publicar(new TransferenciaRealizadaEvent(
                transacao.getId(), origem.getId(), destino.getId(), valor
        ));

        return transacao;
    }
}