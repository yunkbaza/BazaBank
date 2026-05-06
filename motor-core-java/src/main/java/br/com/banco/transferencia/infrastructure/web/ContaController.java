package br.com.banco.transferencia.infrastructure.web;

import br.com.banco.transferencia.application.ports.out.ContaRepositoryPort;
import br.com.banco.transferencia.domain.Conta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.banco.transferencia.domain.SaldoInsuficienteException;
import br.com.banco.transferencia.domain.ValorInvalidoException;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaRepositoryPort contaRepositoryPort;

    public ContaController(ContaRepositoryPort contaRepositoryPort) {
        this.contaRepositoryPort = contaRepositoryPort;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponse> consultarSaldo(@PathVariable UUID id) {
        Optional<Conta> contaBuscada = contaRepositoryPort.buscarPorId(id);

        if (contaBuscada.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Conta conta = contaBuscada.get();
        ContaResponse resposta = new ContaResponse(conta.getId(), conta.getNumero(), conta.getSaldo());

        return ResponseEntity.ok(resposta);
    }
}