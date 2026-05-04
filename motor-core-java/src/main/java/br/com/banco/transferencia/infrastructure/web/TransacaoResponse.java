package br.com.banco.transferencia.infrastructure.web;

import br.com.banco.transferencia.domain.Transacao;

import java.math.BigDecimal;
import java.util.UUID;

public record TransacaoResponse(
        UUID id,
        UUID contaOrigemId,
        UUID contaDestinoId,
        BigDecimal valor,
        String status,
        String dataCriacao // Mudamos de LocalDateTime para String!
) {

    // Factory Method Sênior: Encapsula a conversão de Domínio -> Web
    public static TransacaoResponse fromDomain(Transacao transacao) {
        return new TransacaoResponse(
                transacao.getId(),
                transacao.getContaOrigemId(),
                transacao.getContaDestinoId(),
                transacao.getValor(),
                transacao.getStatus().name(),
                transacao.getDataCriacao() != null ? transacao.getDataCriacao().toString() : null
        );
    }
}