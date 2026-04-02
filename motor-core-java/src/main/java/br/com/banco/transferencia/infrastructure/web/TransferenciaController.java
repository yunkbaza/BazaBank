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

    // Injetamos o UseCase (para escrever) e o Repository (para ler)
    public TransferenciaController(RealizarTransferenciaUseCase realizarTransferenciaUseCase, TransacaoRepositoryPort transacaoRepositoryPort) {
        this.realizarTransferenciaUseCase = realizarTransferenciaUseCase;
        this.transacaoRepositoryPort = transacaoRepositoryPort;
    }

    // Rota POST: Recebe o pedido e aciona as regras de negócio
    @PostMapping
    public ResponseEntity<TransacaoResponse> transferir(@RequestBody TransferenciaRequest request) {
        Transacao transacao = realizarTransferenciaUseCase.executar(
                request.contaOrigemId(),
                request.contaDestinoId(),
                request.valor()
        );

        // Converte a transação de domínio puro para um pacote de resposta (DTO)
        TransacaoResponse response = new TransacaoResponse(
                transacao.getId(),
                transacao.getContaOrigemId(),
                transacao.getContaDestinoId(),
                transacao.getValor(),
                transacao.getStatus().name(),
                transacao.getDataCriacao()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Rota GET: Para podermos consultar o extrato de transferências
    @GetMapping
    public ResponseEntity<List<TransacaoResponse>> listarTodas() {
        List<TransacaoResponse> extrato = transacaoRepositoryPort.buscarTodas()
                .stream()
                .map(t -> new TransacaoResponse(
                        t.getId(),
                        t.getContaOrigemId(),
                        t.getContaDestinoId(),
                        t.getValor(),
                        t.getStatus().name(),
                        t.getDataCriacao()
                ))
                .toList();

        return ResponseEntity.ok(extrato);
    }
}