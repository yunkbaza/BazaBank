package br.com.banco.transferencia.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {

    // O Spring cria automaticamente a query SQL: SELECT * FROM usuarios WHERE cpf = ?
    Optional<UsuarioEntity> findByCpf(String cpf);

}