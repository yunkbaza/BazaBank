package br.com.banco.transferencia.infrastructure.messaging;

import br.com.banco.transferencia.domain.events.TransferenciaRealizadaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoTransferenciaConsumer {

    // 1. Logging Estruturado (Padrão Sénior) em vez de System.out.println
    private static final Logger logger = LoggerFactory.getLogger(NotificacaoTransferenciaConsumer.class);

    private final ObjectMapper objectMapper;

    // Injetamos o Jackson para fazer a desserialização segura do JSON
    public NotificacaoTransferenciaConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "transferencias-realizadas", groupId = "grupo-notificacao-cliente")
    public void consumir(String mensagemJson) {
        try {
            // 2. Desserialização segura (Transforma o JSON num Objeto Java)
            TransferenciaRealizadaEvent evento = objectMapper.readValue(mensagemJson, TransferenciaRealizadaEvent.class);

            // 3. Verificação de Idempotência (Mentalidade de Arquitetura de Software)
            // Em produção, aqui verificaríamos na base de dados se a notificação para este transacaoId já foi enviada.
            logger.info("📩 [KAFKA CONSUMER] Processando notificação para a transação ID: {}", evento.transacaoId());

            // 4. Executa a regra de negócio (Desacoplamento)
            simularEnvioDeEmail(evento);

            logger.info("✅ [SUCESSO] Comprovativo no valor de R$ {} enviado para os clientes das contas {} e {}.",
                    evento.valor(), evento.contaOrigemId(), evento.contaDestinoId());

        } catch (Exception e) {
            // 5. Defesa contra "Poison Pill" (Tratamento de Erros)
            // Impede que o Kafka fique em loop infinito a tentar entregar uma mensagem corrompida.
            logger.error("❌ [ERRO GRAVE] Falha ao processar evento do Kafka. Mensagem original: {}", mensagemJson, e);

            // Aqui, enviaríamos a mensagem para uma DLQ (Dead Letter Queue) para análise manual posterior.
        }
    }

    private void simularEnvioDeEmail(TransferenciaRealizadaEvent evento) throws InterruptedException {
        logger.debug("A comunicar com a API do fornecedor de E-mail (ex: AWS SES, SendGrid)...");
        // Simula a latência de rede que ocorre numa integração real
        Thread.sleep(800);
    }
}