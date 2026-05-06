package br.com.banco.transferencia.infrastructure.security;

import br.com.banco.transferencia.infrastructure.persistence.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository repository;

    public SecurityFilter(TokenService tokenService, UsuarioRepository repository) {
        this.tokenService = tokenService;
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("\n--- INICIANDO FILTRO DE SEGURANÇA ---");
        System.out.println("Endpoint chamado: " + request.getRequestURI());

        var token = recuperarToken(request);
        System.out.println("1. Token chegou no filtro? " + (token != null ? "SIM" : "NÃO"));

        if (token != null) {
            var login = tokenService.validarToken(token);
            System.out.println("2. CPF extraído do token: " + login);

            if (login != null && !login.isEmpty()) {
                var usuario = repository.findByCpf(login);
                System.out.println("3. Usuário encontrado no banco? " + (usuario.isPresent() ? "SIM" : "NÃO"));

                if (usuario.isPresent()) {
                    var permissoes = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                    var autenticacao = new UsernamePasswordAuthenticationToken(
                            usuario.get(),
                            null,
                            permissoes
                    );
                    SecurityContextHolder.getContext().setAuthentication(autenticacao);
                    System.out.println("4. Autenticação injetada com SUCESSO no Spring!");
                }
            } else {
                System.out.println("ALERTA: O TokenService não conseguiu extrair o CPF!");
            }
        }
        System.out.println("--- PASSANDO PARA O PRÓXIMO PASSO ---\n");
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;

        // Garante que tira o "Bearer " com espaço
        return authHeader.replace("Bearer ", "");
    }
}