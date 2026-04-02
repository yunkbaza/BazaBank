package br.com.banco.transferencia.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class Conta {

    private UUID id;
    private String numero;
    private BigDecimal saldo;

    // O Construtor garante que uma conta nunca nasça em um estado inválido
    public Conta(UUID id, String numero, BigDecimal saldoInicial) {
        if (saldoInicial == null || saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O saldo inicial não pode ser negativo");
        }
        this.id = id;
        this.numero = numero;
        this.saldo = saldoInicial;
    }

    // Regras de Negócio (Comportamento)
    public void debitar(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do débito deve ser maior que zero");
        }
        if (this.saldo.compareTo(valor) < 0) {
            throw new IllegalStateException("Saldo insuficiente para a operação");
        }
        this.saldo = this.saldo.subtract(valor);
    }

    public void creditar(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do crédito deve ser maior que zero");
        }
        this.saldo = this.saldo.add(valor);
    }

    // Apenas Getters. Nunca criamos um setSaldo() para não quebrar o encapsulamento.
    public UUID getId() { return id; }
    public String getNumero() { return numero; }
    public BigDecimal getSaldo() { return saldo; }
}