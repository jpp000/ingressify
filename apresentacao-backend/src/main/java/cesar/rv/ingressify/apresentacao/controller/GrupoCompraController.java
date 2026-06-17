package cesar.rv.ingressify.apresentacao.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.grupoCompra.GrupoCompraServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.CriarGrupoCompraRequest;
import cesar.rv.ingressify.apresentacao.dto.GrupoCompraResponse;
import cesar.rv.ingressify.apresentacao.dto.ParticipanteGrupoRequest;
import cesar.rv.ingressify.apresentacao.dto.ParticipanteGrupoResponse;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompraId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupoEntrada;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

@RestController
@RequestMapping("/grupos-compra")
public class GrupoCompraController {

    private final GrupoCompraServicoAplicacao grupoCompraServico;
    private final UsuarioRepositorio usuarioRepositorio;

    public GrupoCompraController(GrupoCompraServicoAplicacao grupoCompraServico, UsuarioRepositorio usuarioRepositorio) {
        this.grupoCompraServico = grupoCompraServico;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @RequestHeader("X-Usuario-Id") int usuarioId,
            @RequestBody CriarGrupoCompraRequest req) {
        if (req.eventoId() == null || req.tipoIngressoId() == null
                || req.prazoPagamento() == null
                || req.participantes() == null || req.participantes().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        for (ParticipanteGrupoRequest p : req.participantes()) {
            boolean semUsuario = p.usuarioId() == null && (p.email() == null || p.email().isBlank());
            if (semUsuario || p.quantidade() == null || p.quantidade() <= 0) {
                return ResponseEntity.badRequest().build();
            }
        }
        try {
            List<ParticipanteGrupoEntrada> entradas = req.participantes().stream()
                    .map(p -> new ParticipanteGrupoEntrada(
                            resolverUsuarioId(p), p.quantidade(),
                            p.meiaEntrada() != null && p.meiaEntrada(), p.documento()))
                    .toList();
            GrupoCompraId id = grupoCompraServico.criar(
                    new EventoId(req.eventoId()),
                    new TipoIngressoId(req.tipoIngressoId()),
                    new UsuarioId(usuarioId),
                    entradas,
                    req.prazoPagamento()).getId();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(GrupoCompraResponse.fromDomain(grupoCompraServico.obter(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("motivo", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        }
    }

    private int resolverUsuarioId(ParticipanteGrupoRequest participante) {
        if (participante.usuarioId() != null) {
            return participante.usuarioId();
        }
        return usuarioRepositorio.obterPorEmail(participante.email().trim()).getId().getId();
    }

    @GetMapping
    public ResponseEntity<List<GrupoCompraResponse>> listar(@RequestParam Integer eventoId) {
        return ResponseEntity.ok(
                grupoCompraServico.listarPorEvento(new EventoId(eventoId)).stream()
                        .map(GrupoCompraResponse::fromDomain)
                        .toList());
    }

    @GetMapping("/meus")
    public ResponseEntity<List<GrupoCompraResponse>> meusGrupos(
            @RequestHeader("X-Usuario-Id") int usuarioId) {
        return ResponseEntity.ok(
                grupoCompraServico.listarMeusGrupos(new UsuarioId(usuarioId)).stream()
                        .map(GrupoCompraResponse::fromDomain)
                        .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoCompraResponse> detalhe(@PathVariable int id) {
        try {
            return ResponseEntity.ok(GrupoCompraResponse.fromDomain(grupoCompraServico.obter(new GrupoCompraId(id))));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/participantes")
    public ResponseEntity<List<ParticipanteGrupoResponse>> participantes(@PathVariable int id) {
        try {
            return ResponseEntity.ok(
                    grupoCompraServico.listarParticipantes(new GrupoCompraId(id)).stream()
                            .map(ParticipanteGrupoResponse::fromDomain)
                            .toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<?> pagar(
            @PathVariable int id,
            @RequestHeader("X-Usuario-Id") int usuarioId) {
        try {
            grupoCompraServico.pagarParticipacao(new GrupoCompraId(id), new UsuarioId(usuarioId));
            return ResponseEntity.ok(GrupoCompraResponse.fromDomain(grupoCompraServico.obter(new GrupoCompraId(id))));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("motivo", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(
            @PathVariable int id,
            @RequestHeader("X-Usuario-Id") int usuarioId) {
        try {
            grupoCompraServico.cancelar(new GrupoCompraId(id), new UsuarioId(usuarioId));
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("motivo", e.getMessage()));
        }
    }
}
