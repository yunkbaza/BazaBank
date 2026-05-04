package br.com.banco.transferencia.infrastructure.web.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class IdempotenciaFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    public IdempotenciaFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Aplica o filtro APENAS nas rotas de criação de transferência (POST)
        if ("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().contains("/transferencias")) {

            // O cliente (Mobile/Web) deve gerar e enviar um UUID único neste Header
            String idempotencyKey = request.getHeader("Idempotency-Key");

            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("O Header 'Idempotency-Key' eh obrigatorio.");
                return;
            }

            String redisKey = "idempotency:" + idempotencyKey;

            // O Segredo Sênior: SETNX (Set If Not Exists) do Redis
            // Esta operação é ATÔMICA. Se 10 requisições idênticas chegarem juntas, só 1 recebe "true".
            Boolean isNovaRequisicao = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, "PROCESSADO", Duration.ofHours(24));

            if (Boolean.FALSE.equals(isNovaRequisicao)) {
                // A chave já estava no Redis. É um duplo clique! Bloqueamos na porta.
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("Requisicao duplicada. Transferencia ja processada ou em andamento.");
                return;
            }
        }

        // Se for uma requisição nova, deixa o código seguir para o Controller -> UseCase
        filterChain.doFilter(request, response);
    }
}