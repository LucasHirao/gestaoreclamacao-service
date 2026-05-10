package com.banco.reclamacoes.bdd;

import io.cucumber.spring.ScenarioScope;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class CenarioContexto {

    private UUID ultimaReclamacaoId;
    private String ultimoProtocolo;

    public UUID ultimaReclamacaoId() {
        return ultimaReclamacaoId;
    }

    public void registrarUltimaReclamacao(final UUID id, final String protocolo) {
        this.ultimaReclamacaoId = id;
        this.ultimoProtocolo = protocolo;
    }

    public String ultimoProtocolo() {
        return ultimoProtocolo;
    }
}
