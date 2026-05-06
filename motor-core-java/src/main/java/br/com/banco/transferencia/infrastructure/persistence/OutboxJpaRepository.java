package br.com.banco.transferencia.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxJpaRepository extends JpaRepository<OutboxEntity, UUID> {

    // O Spring faz a query automática: SELECT * FROM outbox_events WHERE processado = false
    List<OutboxEntity> findByProcessadoFalse();

}