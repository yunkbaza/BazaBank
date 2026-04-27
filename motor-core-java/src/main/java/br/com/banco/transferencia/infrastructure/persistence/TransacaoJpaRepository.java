package br.com.banco.transferencia.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TransacaoJpaRepository extends JpaRepository<TransacaoEntity, UUID> {
    List<TransacaoEntity> findByContaOrigemIdOrContaDestinoIdOrderByDataCriacaoDesc(UUID contaOrigemId, UUID contaDestinoId);
}