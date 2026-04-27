package br.com.banco.transferencia.infrastructure.web;

import br.com.banco.transferencia.application.ports.out.TransacaoRepositoryPort;
import br.com.banco.transferencia.application.usecases.RealizarTransferenciaUseCase;
import br.com.banco.transferencia.domain.Transacao;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    private final RealizarTransferenciaUseCase realizarTransferenciaUseCase;
    private final TransacaoRepositoryPort transacaoRepositoryPort;
    private final StringRedisTemplate redisTemplate; // <-- SÊNIOR: Injeção do Redis em Memória

    public TransferenciaController(RealizarTransferenciaUseCase realizarTransferenciaUseCase,
                                   TransacaoRepositoryPort transacaoRepositoryPort,
                                   StringRedisTemplate redisTemplate) {
        this.realizarTransferenciaUseCase = realizarTransferenciaUseCase;
        this.transacaoRepositoryPort = transacaoRepositoryPort;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping
    public ResponseEntity<?> transferir(
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey, // <-- SÊNIOR: A chave única do Android
            @RequestBody TransferenciaRequest request) {

        // 1. Verificação de Idempotência no Redis (Tempo de resposta < 2ms)
        String redisKey = "PIX_IDEMP:" + idempotencyKey;
        Boolean isNovoPedido = redisTemplate.opsForValue().setIfAbsent(redisKey, "PROCESSANDO", Duration.ofHours(24));

        if (Boolean.FALSE.equals(isNovoPedido)) {
            // Se já existe no Redis, é um duplo clique ou ataque de repetição
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Transação duplicada bloqueada pelo motor de idempotência.");
        }

        try {
            // 2. Executa a transferência normal no PostgreSQL
            Transacao transacao = realizarTransferenciaUseCase.executar(
                    request.contaOrigemId(), request.contaDestinoId(), request.valor()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(TransacaoResponse.fromDomain(transacao));

        } catch (Exception e) {
            // Se a regra de negócio falhar (ex: Saldo Insuficiente), libertamos a chave para o utilizador tentar de novo
            redisTemplate.delete(redisKey);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<TransacaoResponse>> listarTodas() {
        List<TransacaoResponse> extrato = transacaoRepositoryPort.buscarTodas()
                .stream().map(TransacaoResponse::fromDomain).toList();
        return ResponseEntity.ok(extrato);
    }
}