package br.com.banco.transferencia.infrastructure.messaging;

import br.com.banco.transferencia.infrastructure.persistence.OutboxEntity;
import br.com.banco.transferencia.infrastructure.persistence.OutboxJpaRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxRelayWorker {

    private final OutboxJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxRelayWorker(OutboxJpaRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Roda automaticamente a cada 5 segundos
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publicarEventosPendentes() {

        // 1. O Carteiro vai ao banco e pega todas as cartas não enviadas
        List<OutboxEntity> eventosPendentes = outboxRepository.findByProcessadoFalse();

        for (OutboxEntity evento : eventosPendentes) {
            try {
                // 2. Entrega a carta no Kafka
                kafkaTemplate.send(evento.getTopico(), evento.getId().toString(), evento.getPayload());

                // 3. Carimba a carta como "Entregue" (true) e salva no banco
                evento.setProcessado(true);
                outboxRepository.save(evento);

                System.out.println("🚀 [OUTBOX] Evento " + evento.getId() + " enviado para o Kafka no tópico " + evento.getTopico() + "!");

            } catch (Exception e) {
                System.err.println("❌ [OUTBOX] Erro ao enviar evento " + evento.getId() + ": " + e.getMessage());
            }
        }
    }
}