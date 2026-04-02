package br.com.banco.transferencia.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "contas")
public class ContaEntity {

    @Id
    private UUID id;
    private String numero;
    private BigDecimal saldo;

    // O JPA exige um construtor vazio
    public ContaEntity() {}

    public ContaEntity(UUID id, String numero, BigDecimal saldo) {
        this.id = id;
        this.numero = numero;
        this.saldo = saldo;
    }

    // Aqui usamos Getters e Setters normalmente, pois é apenas um DTO de banco
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
}