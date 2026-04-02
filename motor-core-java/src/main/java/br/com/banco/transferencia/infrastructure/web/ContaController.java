package br.com.banco.transferencia.infrastructure.web;

import br.com.banco.transferencia.application.ports.out.ContaRepositoryPort;
import br.com.banco.transferencia.domain.Conta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/contas")
public class ContaController {

    // Agora sim, injetando o seu Port da Clean Architecture corretamente!
    private final ContaRepositoryPort contaRepositoryPort;

    public ContaController(ContaRepositoryPort contaRepositoryPort) {
        this.contaRepositoryPort = contaRepositoryPort;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponse> consultarSaldo(@PathVariable UUID id) {
        Optional<Conta> contaBuscada = contaRepositoryPort.buscarPorId(id);

        if (contaBuscada.isEmpty()) {
            return ResponseEntity.notFound().build(); // Retorna 404 se a conta não existir
        }

        Conta conta = contaBuscada.get();
        ContaResponse resposta = new ContaResponse(conta.getId(), conta.getNumero(), conta.getSaldo());

        return ResponseEntity.ok(resposta); // Retorna 200 OK com o JSON do saldo
    }
}