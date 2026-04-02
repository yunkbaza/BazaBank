package br.com.banco.transferencia.infrastructure.web;

import java.math.BigDecimal;
import java.util.UUID;

// O Record cria getters, construtores e toString automaticamente por debaixo dos panos!
public record TransferenciaRequest(
        UUID contaOrigemId,
        UUID contaDestinoId,
        BigDecimal valor
) {
}