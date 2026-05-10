package com.banco.reclamacoes.infrastructure.web;

import com.banco.reclamacoes.application.query.BuscarReclamacaoPorProtocoloQuery;
import com.banco.reclamacoes.application.query.BuscarReclamacoesFiltrosQuery;
import com.banco.reclamacoes.application.usecase.ConsultarProximasSlaUseCase;
import com.banco.reclamacoes.application.usecase.ConsultarReclamacaoUseCase;
import com.banco.reclamacoes.domain.model.CategoriaReclamacao;
import com.banco.reclamacoes.domain.model.Reclamacao;
import com.banco.reclamacoes.domain.model.StatusReclamacao;
import com.banco.reclamacoes.infrastructure.web.dto.AnexoRespostaDto;
import com.banco.reclamacoes.infrastructure.web.dto.HistoricoLinhaDto;
import com.banco.reclamacoes.infrastructure.web.dto.ReclamacaoRespostaDto;
import com.banco.reclamacoes.infrastructure.web.dto.ReclamacaoResumoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reclamacoes")
@Tag(name = "Reclamações", description = "Consultas do Portal Interno")
@RequiredArgsConstructor
public class ReclamacaoConsultaController {

    private final ConsultarReclamacaoUseCase consultarReclamacaoUseCase;
    private final ConsultarProximasSlaUseCase consultarProximasSlaUseCase;
    private final ReclamacaoDtoFactory reclamacaoDtoFactory;

    @GetMapping("/{protocolo}")
    @Operation(summary = "Consulta reclamação por protocolo")
    public ResponseEntity<ReclamacaoRespostaDto> porProtocolo(@PathVariable final String protocolo) {
        Optional<Reclamacao> reclamacao =
            consultarReclamacaoUseCase.porProtocolo(new BuscarReclamacaoPorProtocoloQuery(protocolo));
        return reclamacao
            .map(r -> ResponseEntity.ok(reclamacaoDtoFactory.completo(r)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Lista reclamações com filtros opcionais")
    public List<ReclamacaoResumoDto> listar(
        @RequestParam(required = false) String clienteId,
        @RequestParam(required = false) StatusReclamacao status,
        @RequestParam(required = false) CategoriaReclamacao categoria,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return consultarReclamacaoUseCase
            .listar(new BuscarReclamacoesFiltrosQuery(clienteId, status, categoria, page, size))
            .stream()
            .map(reclamacaoDtoFactory::resumo)
            .toList();
    }

    @GetMapping("/{protocolo}/historico")
    @Operation(summary = "Consulta histórico interno da reclamação")
    public ResponseEntity<List<HistoricoLinhaDto>> historico(@PathVariable final String protocolo) {
        return consultarReclamacaoUseCase
            .porProtocolo(new BuscarReclamacaoPorProtocoloQuery(protocolo))
            .map(
                r ->
                    ResponseEntity.ok(
                        r.historicoInterno().stream()
                            .map(h -> new HistoricoLinhaDto(h.tipo(), h.detalhe(), h.ocorridoEm()))
                            .toList()))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{protocolo}/anexos")
    @Operation(summary = "Consulta metadados de anexos aprovados no MVP")
    public ResponseEntity<List<AnexoRespostaDto>> anexos(@PathVariable final String protocolo) {
        Optional<Reclamacao> reclamacao =
            consultarReclamacaoUseCase.porProtocolo(new BuscarReclamacaoPorProtocoloQuery(protocolo));
        return reclamacao
            .map(
                r ->
                    ResponseEntity.ok(
                        r.anexos().stream()
                            .map(a -> new AnexoRespostaDto(a.referencia(), a.tipoMime(), a.atributos()))
                            .toList()))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/sla/proximas-do-vencimento")
    @Operation(summary = "Lista reclamações com deadline próximo conforme horizonte configurado")
    public List<ReclamacaoResumoDto> proximasSla(
        @RequestParam(defaultValue = "72") long horizonteHoras,
        @RequestParam(defaultValue = "100") int limite
    ) {
        return consultarProximasSlaUseCase.executar(horizonteHoras, limite).stream()
            .map(reclamacaoDtoFactory::resumo)
            .toList();
    }
}
