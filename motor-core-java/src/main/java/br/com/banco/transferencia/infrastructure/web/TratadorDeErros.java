package br.com.banco.transferencia.infrastructure.web; // O IntelliJ deve arrumar isso sozinho no Refactor

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroResposta> tratarRegraDeNegocio(IllegalStateException ex) {
        ErroResposta erro = new ErroResposta(ex.getMessage());
        return ResponseEntity.badRequest().body(erro); // Status 400
    }

    public record ErroResposta(String mensagem) {}
}