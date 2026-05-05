package br.com.banco.transferencia.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxJpaRepository extends JpaRepository<OutboxEntity, UUID> {

    // Método mágico do Spring Data para buscar apenas os eventos que ainda não foram para o Kafka
    List<OutboxEntity> findByProcessadoFalseOrderByDataCriacaoAsc();
}