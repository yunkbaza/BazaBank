package br.com.banco.transferencia.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // O nome do tópico do Kafka para onde esta mensagem deve ir
    private String topico;

    // O conteúdo da mensagem (o JSON do evento)
    @Column(columnDefinition = "TEXT")
    private String payload;

    private LocalDateTime dataCriacao;

    // Flag para sabermos se já foi enviado para o Kafka ou não
    private boolean processado;

    public OutboxEntity() {}

    public OutboxEntity(String topico, String payload) {
        this.topico = topico;
        this.payload = payload;
        this.dataCriacao = LocalDateTime.now();
        this.processado = false; // Nasce sempre como não processado
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTopico() { return topico; }
    public void setTopico(String topico) { this.topico = topico; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public boolean isProcessado() { return processado; }
    public void setProcessado(boolean processado) { this.processado = processado; }
}