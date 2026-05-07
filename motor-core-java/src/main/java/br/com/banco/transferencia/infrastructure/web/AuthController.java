package br.com.banco.transferencia.infrastructure.web;

import br.com.banco.transferencia.infrastructure.persistence.UsuarioEntity;
import br.com.banco.transferencia.infrastructure.persistence.UsuarioRepository;
import br.com.banco.transferencia.infrastructure.persistence.ContaEntity;
import br.com.banco.transferencia.infrastructure.persistence.ContaJpaRepository;
import br.com.banco.transferencia.infrastructure.security.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository repository;
    private final ContaJpaRepository contaRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthController(UsuarioRepository repository, ContaJpaRepository contaRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.repository = repository;
        this.contaRepository = contaRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    // 1. ROTA DE REGISTRO
    @PostMapping("/registrar")
    public ResponseEntity<String> registrar(@RequestBody AuthRequest request) {
        if (repository.findByCpf(request.cpf()).isPresent()) {
            return ResponseEntity.badRequest().body("Erro: CPF já cadastrado!");
        }

        String senhaCriptografada = passwordEncoder.encode(request.senha());
        UsuarioEntity novoUsuario = new UsuarioEntity(request.cpf(), senhaCriptografada);
        repository.save(novoUsuario);

        ContaEntity novaConta = new ContaEntity();
        novaConta.setId(UUID.randomUUID());
        novaConta.setNumero(request.cpf());
        novaConta.setSaldo(BigDecimal.ZERO);
        contaRepository.save(novaConta);

        return ResponseEntity.status(HttpStatus.CREATED).body("Conta criada com sucesso!");
    }

    // 2. ROTA DE LOGIN
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody AuthRequest request) {
        // 🔥 CORREÇÃO: Primeiro procuramos o Utilizador pelo CPF que veio no request!
        var usuarioOpt = repository.findByCpf(request.cpf());

        if (usuarioOpt.isPresent()) {
            var usuario = usuarioOpt.get();
            if (passwordEncoder.matches(request.senha(), usuario.getSenha())) {
                String token = tokenService.gerarToken(usuario.getCpf());

                // 🔥 CORREÇÃO: Agora sim, procuramos a conta bancária desse utilizador!
                var contaOpt = contaRepository.findByNumero(usuario.getCpf());
                String contaId = contaOpt.isPresent() ? contaOpt.get().getId().toString() : "";

                return ResponseEntity.ok(new TokenResponse(token, usuario.getCpf(), contaId));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}

record AuthRequest(String cpf, String senha) {}
record TokenResponse(String token, String cpf, String contaId) {}