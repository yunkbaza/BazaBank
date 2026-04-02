package br.com.banco.transferencia.infrastructure.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// O pacote de dados que vai aparecer na tela do Postman
public record TransacaoResponse(
        UUID id,
        UUID contaOrigemId,
        UUID contaDestinoId,
        BigDecimal valor,
        String status,
        LocalDateTime dataCriacao
) {}