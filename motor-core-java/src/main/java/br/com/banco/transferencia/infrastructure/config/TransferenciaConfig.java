package br.com.banco.transferencia.infrastructure.config;

import br.com.banco.transferencia.application.ports.out.ContaRepositoryPort;
import br.com.banco.transferencia.application.ports.out.TransacaoRepositoryPort;
import br.com.banco.transferencia.application.ports.out.TransferenciaEventPublisherPort;
import br.com.banco.transferencia.application.usecases.RealizarTransferenciaUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TransferenciaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RealizarTransferenciaUseCase realizarTransferenciaUseCase(
            ContaRepositoryPort contaRepositoryPort,
            TransacaoRepositoryPort transacaoRepositoryPort,
            TransferenciaEventPublisherPort eventPublisher
    ) {
        return new RealizarTransferenciaUseCase(contaRepositoryPort, transacaoRepositoryPort, eventPublisher);
    }

    // ==========================================
    // CONFIGURAÇÃO KAFKA NATIVA E DESACOPLADA
    // ==========================================
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Usamos o StringSerializer nativo da Apache para TUDO (Chave e Valor)
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // O Template agora é focado 100% no envio de Strings
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}