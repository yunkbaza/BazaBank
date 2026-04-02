package br.com.banco.transferencia.application.ports.out;

import br.com.banco.transferencia.domain.events.TransferenciaRealizadaEvent;

public interface TransferenciaEventPublisherPort {
    void publicar(TransferenciaRealizadaEvent event);


}