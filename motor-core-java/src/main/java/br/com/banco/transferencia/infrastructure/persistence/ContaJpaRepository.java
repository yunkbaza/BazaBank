package br.com.banco.transferencia.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ContaJpaRepository extends JpaRepository<ContaEntity, UUID> {

    // O Pessimistic Write gera um "SELECT ... FOR UPDATE" no PostgreSQL
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ContaEntity c WHERE c.id = :id")
    Optional<ContaEntity> findByIdComLock(@Param("id") UUID id);
}