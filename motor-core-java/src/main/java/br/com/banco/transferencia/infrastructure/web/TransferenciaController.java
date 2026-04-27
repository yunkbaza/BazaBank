package br.com.banco.transferencia.infrastructure.web;

import br.com.banco.transferencia.application.ports.out.TransacaoRepositoryPort;
import br.com.banco.transferencia.application.usecases.RealizarTransferenciaUseCase;
import br.com.banco.transferencia.domain.Transacao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    private final RealizarTransferenciaUseCase realizarTransferenciaUseCase;
    private final TransacaoRepositoryPort transacaoRepositoryPort;

    public TransferenciaController(RealizarTransferenciaUseCase realizarTransferenciaUseCase, TransacaoRepositoryPort transacaoRepositoryPort) {
        this.realizarTransferenciaUseCase = realizarTransferenciaUseCase;
        this.transacaoRepositoryPort = transacaoRepositoryPort;
    }

    @PostMapping
    public ResponseEntity<TransacaoResponse> transferir(@RequestBody TransferenciaRequest request) {
        Transacao transacao = realizarTransferenciaUseCase.executar(
                request.contaOrigemId(),
                request.contaDestinoId(),
                request.valor()
        );

        // Mapeamento limpo usando Factory Method
        return ResponseEntity.status(HttpStatus.CREATED).body(TransacaoResponse.fromDomain(transacao));
    }

    @GetMapping
    public ResponseEntity<List<TransacaoResponse>> listarTodas() {
        // DICA SÊNIOR: Em produção, utilize Pageable (org.springframework.data.domain.Pageable)
        // para evitar OutOfMemory (OOM) em contas com muitas transações.
        List<TransacaoResponse> extrato = transacaoRepositoryPort.buscarTodas()
                .stream()
                .map(TransacaoResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(extrato);
    }
}