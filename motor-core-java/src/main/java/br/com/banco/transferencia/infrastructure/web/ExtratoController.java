package br.com.banco.transferencia.infrastructure.web;

import br.com.banco.transferencia.infrastructure.persistence.TransacaoEntity;
import br.com.banco.transferencia.infrastructure.persistence.TransacaoJpaRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/contas")
public class ExtratoController {

    private static final Logger log = LoggerFactory.getLogger(ExtratoController.class);
    private final TransacaoJpaRepository repository;

    public ExtratoController(TransacaoJpaRepository repository) {
        this.repository = repository;
    }

    public record TransacaoExtratoDTO(String id, String contaOrigemId, String contaDestinoId, java.math.BigDecimal valor, String dataCriacao) {}

    // 🔥 O Endpoint agora tem Paginação (page, size) e o Circuit Breaker ativado!
    @GetMapping("/{contaId}/extrato")
    @CircuitBreaker(name = "extratoCB", fallbackMethod = "fallbackObterExtrato")
    public ResponseEntity<List<TransacaoExtratoDTO>> obterExtrato(
            @PathVariable String contaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size // Traz 20 movimentos por vez
    ) {
        UUID idBusca = UUID.fromString(contaId);
        Pageable paginacao = PageRequest.of(page, size);

        // Busca paginada no banco
        Page<TransacaoEntity> paginaTransacoes = repository.findByContaOrigemIdOrContaDestinoIdOrderByDataCriacaoDesc(idBusca, idBusca, paginacao);

        List<TransacaoExtratoDTO> resposta = paginaTransacoes.stream()
                .map(t -> new TransacaoExtratoDTO(
                        t.getId().toString(),
                        t.getContaOrigemId().toString(),
                        t.getContaDestinoId().toString(),
                        t.getValor(),
                        t.getDataCriacao() != null ? t.getDataCriacao().toString() : ""
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(resposta);
    }

    // 🔥 O FALLBACK: Se o banco de dados cair ou ficar lento, este método é chamado automaticamente!
    public ResponseEntity<List<TransacaoExtratoDTO>> fallbackObterExtrato(String contaId, int page, int size, Throwable exception) {
        log.error("Circuit Breaker ABERTO para a conta {}. Motivo: {}", contaId, exception.getMessage());

        // Em vez de dar erro 500 no telemóvel, devolvemos 503 (Serviço Indisponível)
        // com uma lista vazia ou podíamos devolver dados em cache.
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Collections.emptyList());
    }
}