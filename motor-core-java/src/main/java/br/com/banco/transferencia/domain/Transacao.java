package br.com.banco.transferencia.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transacao {

    private UUID id;
    private UUID contaOrigemId;
    private UUID contaDestinoId;
    private BigDecimal valor;
    private StatusTransacao status;
    private LocalDateTime dataCriacao;

    // CONSTRUTOR 1: Toda transação NOVA já nasce "Pendente" por padrão (Usado pelo UseCase)
    public Transacao(UUID id, UUID contaOrigemId, UUID contaDestinoId, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
        }
        if (contaOrigemId.equals(contaDestinoId)) {
            throw new IllegalArgumentException("A conta de origem e destino não podem ser a mesma.");
        }

        this.id = id;
        this.contaOrigemId = contaOrigemId;
        this.contaDestinoId = contaDestinoId;
        this.valor = valor;
        this.status = StatusTransacao.PENDENTE;
        this.dataCriacao = LocalDateTime.now();
    }

    // CONSTRUTOR 2: Exclusivo para o Repository "ressuscitar" os dados do banco
    public Transacao(UUID id, UUID contaOrigemId, UUID contaDestinoId, BigDecimal valor, StatusTransacao status, LocalDateTime dataCriacao) {
        this.id = id;
        this.contaOrigemId = contaOrigemId;
        this.contaDestinoId = contaDestinoId;
        this.valor = valor;
        this.status = status;
        this.dataCriacao = dataCriacao;
    }

    // Máquina de Estado: Protegendo a transição de status
    public void completar() {
        if (this.status != StatusTransacao.PENDENTE) {
            throw new IllegalStateException("Apenas transações pendentes podem ser completadas.");
        }
        this.status = StatusTransacao.SUCESSO;
    }

    public void falhar() {
        if (this.status != StatusTransacao.PENDENTE) {
            throw new IllegalStateException("Apenas transações pendentes podem falhar.");
        }
        this.status = StatusTransacao.FALHA;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getContaOrigemId() { return contaOrigemId; }
    public UUID getContaDestinoId() { return contaDestinoId; }
    public BigDecimal getValor() { return valor; }
    public StatusTransacao getStatus() { return status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
}