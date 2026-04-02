package br.com.banco.transferencia.infrastructure.web; // Ajuste o pacote se precisar

import java.math.BigDecimal;
import java.util.UUID;

// O 'record' do Java é perfeito para DTOs: imutável, leve e rápido!
public record ContaResponse(UUID id, String numero, BigDecimal saldo) {
}