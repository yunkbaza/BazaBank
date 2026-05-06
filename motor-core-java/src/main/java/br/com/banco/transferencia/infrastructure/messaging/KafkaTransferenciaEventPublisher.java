package br.com.banco.transferencia.infrastructure.messaging;

import br.com.banco.transferencia.application.ports.out.TransferenciaEventPublisherPort;
import br.com.banco.transferencia.domain.events.TransferenciaRealizadaEvent;
import br.com.banco.transferencia.infrastructure.persistence.OutboxEntity;
import br.com.banco.transferencia.infrastructure.persistence.OutboxJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class KafkaTransferenciaEventPublisher implements TransferenciaEventPublisherPort {

    private final OutboxJpaRepository outboxRepository; // Agora injetamos o banco, não o KafkaTemplate
    private final ObjectMapper objectMapper;

    public KafkaTransferenciaEventPublisher(OutboxJpaRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(TransferenciaRealizadaEvent event) {
        try {
            // 1. Converte o objeto do domínio num JSON seguro
            String payload = objectMapper.writeValueAsString(event);

            // 2. PADRÃO OUTBOX: Salva na tabela do banco de dados em vez de enviar para o Kafka
            // Isso garante que se o banco der rollback na transferência, o evento também não será salvo.
            OutboxEntity outbox = new OutboxEntity(
                    "transferencias-realizadas", // Tópico destino
                    payload
            );

            outboxRepository.save(outbox);
            System.out.println("📦 Evento guardado na Outbox: Transação " + event.transacaoId());

        } catch (Exception e) {
            // Se a serialização falhar, a transação da transferência inteira sofrerá rollback
            throw new RuntimeException("Erro grave ao tentar registrar o evento no Outbox", e);
        }
    }
}