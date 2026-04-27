package br.com.banco.transferencia.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // O CPF será o nosso "Username". Tem de ser único!
    @Column(unique = true, nullable = false)
    private String cpf;

    @Column(nullable = false)
    private String senha;

    // Construtor Vazio (Exigência do JPA)
    public UsuarioEntity() {}

    public UsuarioEntity(String cpf, String senha) {
        this.cpf = cpf;
        this.senha = senha;
    }

    // Getters
    public UUID getId() { return id; }
    public String getCpf() { return cpf; }
    public String getSenha() { return senha; }
}