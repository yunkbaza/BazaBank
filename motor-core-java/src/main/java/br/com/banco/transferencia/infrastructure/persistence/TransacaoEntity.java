package br.com.banco.transferencia.infrastructure.persistence;

import br.com.banco.transferencia.domain.StatusTransacao;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transacoes")
public class TransacaoEntity {

    @Id
    private UUID id;
    private UUID contaOrigemId;
    private UUID contaDestinoId;
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private StatusTransacao status;

    // A MÁGICA ESTÁ AQUI: Ensina o Jackson a converter a data para texto no GET
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataCriacao;

    // Construtor vazio obrigatório do JPA
    public TransacaoEntity() {}

    public TransacaoEntity(UUID id, UUID contaOrigemId, UUID contaDestinoId, BigDecimal valor, StatusTransacao status, LocalDateTime dataCriacao) {
        this.id = id;
        this.contaOrigemId = contaOrigemId;
        this.contaDestinoId = contaDestinoId;
        this.valor = valor;
        this.status = status;
        this.dataCriacao = dataCriacao;
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getContaOrigemId() { return contaOrigemId; }
    public void setContaOrigemId(UUID contaOrigemId) { this.contaOrigemId = contaOrigemId; }
    public UUID getContaDestinoId() { return contaDestinoId; }
    public void setContaDestinoId(UUID contaDestinoId) { this.contaDestinoId = contaDestinoId; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public StatusTransacao getStatus() { return status; }
    public void setStatus(StatusTransacao status) { this.status = status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}