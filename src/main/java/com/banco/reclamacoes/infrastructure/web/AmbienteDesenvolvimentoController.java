package com.banco.reclamacoes.infrastructure.web;

import com.banco.reclamacoes.application.port.input.RegistrarSolicitacaoPadronizadaPort;
import com.banco.reclamacoes.domain.port.MotorClassificacaoPort;
import com.banco.reclamacoes.domain.valueobject.DescricaoReclamacao;
import com.banco.reclamacoes.infrastructure.web.dto.dev.ClassificarTextoRequisicaoDto;
import com.banco.reclamacoes.infrastructure.web.dto.dev.ClassificarTextoRespostaDto;
import com.banco.reclamacoes.infrastructure.web.dto.dev.SolicitacaoPadronizadaInboundDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dev")
@Profile({"local", "dev"})
@Tag(name = "Desenvolvimento", description = "Endpoints apenas para profiles local/dev")
public class AmbienteDesenvolvimentoController {

    private final RegistrarSolicitacaoPadronizadaPort registrarSolicitacaoPadronizadaPort;
    private final MotorClassificacaoPort motorClassificacaoPort;

    public AmbienteDesenvolvimentoController(
        final RegistrarSolicitacaoPadronizadaPort registrarSolicitacaoPadronizadaPort,
        final MotorClassificacaoPort motorClassificacaoPort) {
        this.registrarSolicitacaoPadronizadaPort = registrarSolicitacaoPadronizadaPort;
        this.motorClassificacaoPort = motorClassificacaoPort;
    }

    @PostMapping("/simular-solicitacao-padronizada")
    @Operation(summary = "Simula processamento de solicitação padronizada (mesmo fluxo do consumer)")
    public ResponseEntity<UUID> simular(@RequestBody final SolicitacaoPadronizadaInboundDto payload) {
        Optional<UUID> resultado = registrarSolicitacaoPadronizadaPort.processar(payload.paraComando());
        return resultado.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/classificar-texto")
    @Operation(summary = "Explica classificação automática para um texto arbitrário")
    public ClassificarTextoRespostaDto classificar(@RequestBody final ClassificarTextoRequisicaoDto texto) {
        var resultado =
            motorClassificacaoPort.classificar(DescricaoReclamacao.de(texto.texto()));
        return ClassificarTextoRespostaDto.deduzido(resultado);
    }
}
