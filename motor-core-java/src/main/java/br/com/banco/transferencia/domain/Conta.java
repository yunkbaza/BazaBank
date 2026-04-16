package br.com.banco.transferencia.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class Conta {
    private UUID id;
    private String numero;
    private BigDecimal saldo;

    public Conta(UUID id, String numero, BigDecimal saldoInicial) {
        if (saldoInicial == null || saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValorInvalidoException("Saldo inicial não pode ser negativo");
        }
        this.id = id;
        this.numero = numero;
        this.saldo = saldoInicial;
    }

    public void debitar(BigDecimal valor) {
        validarValor(valor, "débito");
        if (this.saldo.compareTo(valor) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente");
        }
        this.saldo = this.saldo.subtract(valor);
    }

    public void creditar(BigDecimal valor) {
        validarValor(valor, "crédito");
        this.saldo = this.saldo.add(valor);
    }

    private void validarValor(BigDecimal valor, String op) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorInvalidoException("Valor de " + op + " deve ser positivo");
        }
    }

    public UUID getId() { return id; }
    public BigDecimal getSaldo() { return saldo; }
}