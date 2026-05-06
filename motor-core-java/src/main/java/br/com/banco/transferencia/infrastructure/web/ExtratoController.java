package br.com.banco.transferencia.infrastructure.web;

import br.com.banco.transferencia.infrastructure.persistence.TransacaoEntity;
import br.com.banco.transferencia.infrastructure.persistence.TransacaoJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/contas")
public class ExtratoController {

    private final TransacaoJpaRepository repository;

    public ExtratoController(TransacaoJpaRepository repository) {
        this.repository = repository;
    }

    // Criamos um pequeno "molde" (DTO) para o Jackson não chorar com a data
    public record TransacaoExtratoDTO(String id, String contaOrigemId, String contaDestinoId, java.math.BigDecimal valor, String dataCriacao) {}

    @GetMapping("/{contaId}/extrato")
    public ResponseEntity<List<TransacaoExtratoDTO>> obterExtrato(@PathVariable String contaId) {
        UUID idBusca = UUID.fromString(contaId);

        List<TransacaoEntity> transacoes = repository.findByContaOrigemIdOrContaDestinoIdOrderByDataCriacaoDesc(idBusca, idBusca);

        // Convertendo as Entidades para os "Moldes" seguros (transformando a data numa String)
        List<TransacaoExtratoDTO> resposta = transacoes.stream()
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
}