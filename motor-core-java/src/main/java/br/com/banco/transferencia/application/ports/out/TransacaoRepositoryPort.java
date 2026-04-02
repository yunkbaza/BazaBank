package br.com.banco.transferencia.application.ports.out;

import br.com.banco.transferencia.domain.Transacao;
import java.util.List; // <-- Faltava importar a Lista do Java

public interface TransacaoRepositoryPort {

    void salvar(Transacao transacao);

    List<Transacao> buscarTodas();

}