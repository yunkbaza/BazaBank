package br.com.banco.transferencia.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TransacaoJpaRepository extends JpaRepository<TransacaoEntity, UUID> {

    Page<TransacaoEntity> findByContaOrigemIdOrContaDestinoIdOrderByDataCriacaoDesc(
            UUID contaOrigemId,
            UUID contaDestinoId,
            Pageable pageable
    );
}