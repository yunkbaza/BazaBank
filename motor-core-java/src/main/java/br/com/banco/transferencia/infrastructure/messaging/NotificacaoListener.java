package br.com.banco.transferencia.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoListener {

    // Fica à escuta no exato tópico onde o nosso Outbox publica as mensagens
    @KafkaListener(topics = "transferencias-realizadas", groupId = "grupo-notificacao-cliente")
    public void consumirNotificacao(String mensagemJson) {
        System.out.println("\n=======================================================");
        System.out.println("🔔 [NOTIFICAÇÃO] Nova transferência detectada no Kafka!");
        System.out.println("📩 Conteúdo do Evento: " + mensagemJson);
        System.out.println("📱 Simulando envio de Push Notification para o telemóvel do cliente...");
        System.out.println("✅ Notificação enviada com sucesso!");
        System.out.println("=======================================================\n");
    }
}