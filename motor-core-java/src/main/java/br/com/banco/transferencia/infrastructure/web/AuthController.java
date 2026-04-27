package br.com.banco.transferencia.infrastructure.web;

import br.com.banco.transferencia.infrastructure.persistence.UsuarioEntity;
import br.com.banco.transferencia.infrastructure.persistence.UsuarioRepository;
import br.com.banco.transferencia.infrastructure.security.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthController(UsuarioRepository repository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    // 1. ROTA DE REGISTRO
    @PostMapping("/registrar")
    public ResponseEntity<String> registrar(@RequestBody AuthRequest request) {
        // Verifica se o utilizador já existe
        if (repository.findByCpf(request.cpf()).isPresent()) {
            return ResponseEntity.badRequest().body("Erro: CPF já cadastrado!");
        }

        // Magia Sênior: Nunca salvamos a senha em texto limpo! Encriptamos com BCrypt.
        String senhaCriptografada = passwordEncoder.encode(request.senha());
        UsuarioEntity novoUsuario = new UsuarioEntity(request.cpf(), senhaCriptografada);

        repository.save(novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body("Conta criada com sucesso!");
    }

    // 2. ROTA DE LOGIN
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody AuthRequest request) {
        var usuarioOpt = repository.findByCpf(request.cpf());

        if (usuarioOpt.isPresent()) {
            var usuario = usuarioOpt.get();
            // O BCrypt verifica se a senha digitada bate com a hash gigante guardada no banco
            if (passwordEncoder.matches(request.senha(), usuario.getSenha())) {
                // Se bater, geramos o Crachá (Token JWT)!
                String token = tokenService.gerarToken(usuario.getCpf());
                return ResponseEntity.ok(new TokenResponse(token, usuario.getCpf()));
            }
        }
        // Se errar a senha ou o CPF não existir, devolvemos 401 (Não Autorizado)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}

// DTOs (Data Transfer Objects) usando Records do Java
record AuthRequest(String cpf, String senha) {}
record TokenResponse(String token, String cpf) {}