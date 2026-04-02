package br.com.banco.transferencia.infrastructure.persistence;

import br.com.banco.transferencia.application.ports.out.TransacaoRepositoryPort;
import br.com.banco.transferencia.domain.Transacao;
import org.springframework.stereotype.Repository;

import java.util.List; // Importação da Lista adicionada

@Repository
public class TransacaoRepositoryAdapter implements TransacaoRepositoryPort {

    private final TransacaoJpaRepository jpaRepository;

    public TransacaoRepositoryAdapter(TransacaoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void salvar(Transacao transacao) {
        TransacaoEntity entity = new TransacaoEntity(
                transacao.getId(),
                transacao.getContaOrigemId(),
                transacao.getContaDestinoId(),
                transacao.getValor(),
                transacao.getStatus(),
                transacao.getDataCriacao()
        );
        jpaRepository.save(entity);
    }

    @Override
    public List<Transacao> buscarTodas() {
        return jpaRepository.findAll().stream()
                .map(entity -> new Transacao(
                        entity.getId(),
                        entity.getContaOrigemId(),
                        entity.getContaDestinoId(),
                        entity.getValor(),
                        entity.getStatus(),
                        entity.getDataCriacao()
                ))
                .toList();
    }
}