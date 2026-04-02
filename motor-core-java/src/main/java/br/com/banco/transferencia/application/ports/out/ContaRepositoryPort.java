package br.com.banco.transferencia.application.ports.out;

import br.com.banco.transferencia.domain.Conta;
import java.util.Optional;
import java.util.UUID;

public interface ContaRepositoryPort {

    // Busca simples (leitura rápida, sem bloquear a linha no banco)
    Optional<Conta> buscarPorId(UUID id);

    // Nova busca com Pessimistic Lock (bloqueia a linha para concorrência)
    Optional<Conta> buscarPorIdComLock(UUID id);

    void salvar(Conta conta);
}