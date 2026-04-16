package br.com.banco.transferencia.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class Conta {

    private UUID id;
    private String numero;
    private BigDecimal saldo;

    public Conta(UUID id, String numero, BigDecimal saldoInicial) {
        if (saldoInicial == null || saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValorInvalidoException("O saldo inicial não pode ser negativo");
        }
        this.id = id;
        this.numero = numero;
        this.saldo = saldoInicial;
    }

    public void debitar(BigDecimal valor) {
        validarValor(valor, "débito");
        if (this.saldo.compareTo(valor) < 0) {
            // Exceção específica de negócio! Facilita o mapeamento para HTTP 422
            throw new SaldoInsuficienteException("Saldo insuficiente para a operação");
        }
        this.saldo = this.saldo.subtract(valor);
    }

    public void creditar(BigDecimal valor) {
        validarValor(valor, "crédito");
        this.saldo = this.saldo.add(valor);
    }

    private void validarValor(BigDecimal valor, String operacao) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorInvalidoException("O valor do " + operacao + " deve ser maior que zero");
        }
    }

    public UUID getId() { return id; }
    public String getNumero() { return numero; }
    public BigDecimal getSaldo() { return saldo; }
}

    }