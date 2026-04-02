package br.com.banco.transferencia.infrastructure.persistence;

import br.com.banco.transferencia.application.ports.out.ContaRepositoryPort;
import br.com.banco.transferencia.domain.Conta;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ContaRepositoryAdapter implements ContaRepositoryPort {

    private final ContaJpaRepository contaJpaRepository;

    public ContaRepositoryAdapter(ContaJpaRepository contaJpaRepository) {
        this.contaJpaRepository = contaJpaRepository;
    }

    @Override
    public Optional<Conta> buscarPorId(UUID id) {
        // Converte de ENTITY (Banco) para CONTA (Domínio)
        return contaJpaRepository.findById(id)
                .map(entity -> new Conta(
                        entity.getId(),
                        entity.getNumero(),
                        entity.getSaldo()
                ));
    }

    @Override
    public void salvar(Conta conta) {
        // Converte de CONTA (Domínio) para ENTITY (Banco) para o JPA conseguir salvar
        ContaEntity entity = new ContaEntity();
        entity.setId(conta.getId());
        entity.setNumero(conta.getNumero());
        entity.setSaldo(conta.getSaldo());

        contaJpaRepository.save(entity);
    }

    @Override
    public Optional<Conta> buscarPorIdComLock(UUID id) {
        return contaJpaRepository.findByIdComLock(id)
                .map(entity -> new Conta(
                        entity.getId(),
                        entity.getNumero(),
                        entity.getSaldo()
                ));
    }

}