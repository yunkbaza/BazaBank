package br.com.banco.transferencia.infrastructure.messaging;

import br.com.banco.transferencia.application.ports.out.TransferenciaEventPublisherPort;
import br.com.banco.transferencia.domain.events.TransferenciaRealizadaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaTransferenciaEventPublisher implements TransferenciaEventPublisherPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper; // <-- O nosso conversor JSON

    public KafkaTransferenciaEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(TransferenciaRealizadaEvent event) {
        try {
            // 1. Converte o objeto do domínio num JSON seguro
            String mensagemJson = objectMapper.writeValueAsString(event);

            // 2. Envia a String pura para o tópico
            kafkaTemplate.send("transferencias-realizadas", event.transacaoId().toString(), mensagemJson);
            System.out.println("🚀 Evento publicado no Kafka: Transação " + event.transacaoId());

        } catch (Exception e) {
            // Proteção da arquitetura: se a conversão falhar, não calamos o erro
            throw new RuntimeException("Erro grave ao tentar serializar o evento de transferência", e);
        }
    }
}