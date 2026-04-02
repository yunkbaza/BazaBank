package br.com.banco.transferencia.domain.events;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferenciaRealizadaEvent(
        UUID transacaoId,
        UUID contaOrigemId,
        UUID contaDestinoId,
        BigDecimal valor
) {}