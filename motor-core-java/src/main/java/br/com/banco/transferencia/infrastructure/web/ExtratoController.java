// 1. Erro do pacote resolvido (agora aponta para o caminho exato da sua pasta!)
package br.com.banco.transferencia.infrastructure.web;

import br.com.banco.transferencia.infrastructure.persistence.TransacaoEntity;
import br.com.banco.transferencia.infrastructure.persistence.TransacaoJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID; // 2. Erro do UUID resolvido (import adicionado!)

@RestController
@RequestMapping("/api/contas/{contaId}/extrato")
public class ExtratoController {

    private final TransacaoJpaRepository repository;

    public ExtratoController(TransacaoJpaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<TransacaoEntity>> obterExtrato(@PathVariable String contaId) {
        // Converte a String do Android para UUID
        UUID idBusca = UUID.fromString(contaId);

        // Faz a busca na base de dados
        List<TransacaoEntity> transacoes = repository.findByContaOrigemIdOrContaDestinoIdOrderByDataCriacaoDesc(idBusca, idBusca);

        return ResponseEntity.ok(transacoes);
    }
}