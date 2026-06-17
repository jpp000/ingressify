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

import cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos.CompraAssentoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos.MapaAssentosServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.BloquearAssentosRequest;
import cesar.rv.ingressify.apresentacao.dto.AssentoResponse;
import cesar.rv.ingressify.apresentacao.dto.CompraAssentoResponse;
import cesar.rv.ingressify.apresentacao.dto.ComprarAssentosRequest;
import cesar.rv.ingressify.apresentacao.dto.CriarMapaAssentosRequest;
import cesar.rv.ingressify.apresentacao.dto.MapaAssentosResponse;
import cesar.rv.ingressify.apresentacao.dto.ReservarAssentosRequest;
import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.Assento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.AssentoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentos;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;

@RestController
@RequestMapping("/mapas-assentos")
public class MapaAssentosController {

    private final MapaAssentosServicoAplicacao mapaServico;
    private final CompraAssentoServicoAplicacao compraServico;

    public MapaAssentosController(MapaAssentosServicoAplicacao mapaServico,
            CompraAssentoServicoAplicacao compraServico) {
        this.mapaServico = mapaServico;
        this.compraServico = compraServico;
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @RequestHeader("X-Usuario-Id") int usuarioId,
            @RequestBody CriarMapaAssentosRequest req) {
        if (req.eventoId() == null || req.totalLinhas() == null || req.totalColunas() == null
                || req.precoNormal() == null || req.precoVip() == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            MapaAssentosId id = mapaServico.criarMapa(
                    new EventoId(req.eventoId()),
                    req.totalLinhas(), req.totalColunas(),
                    req.precoNormal(), req.precoVip(),
                    new UsuarioId(usuarioId));
            MapaAssentos mapa = mapaServico.obterPorEvento(new EventoId(req.eventoId()));
            List<Assento> assentos = mapaServico.listarAssentos(id);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(MapaAssentosResponse.fromDomain(mapa, assentos));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("motivo", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> obterPorEvento(@RequestParam Integer eventoId) {
        try {
            MapaAssentos mapa = mapaServico.obterPorEvento(new EventoId(eventoId));
            List<Assento> assentos = mapaServico.listarAssentos(mapa.getId());
            return ResponseEntity.ok(MapaAssentosResponse.fromDomain(mapa, assentos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/assentos")
    public ResponseEntity<List<AssentoResponse>> listarAssentos(@PathVariable int id) {
        try {
            return ResponseEntity.ok(
                    mapaServico.listarAssentos(new MapaAssentosId(id)).stream()
                            .map(AssentoResponse::fromDomain)
                            .toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/reservar")
    public ResponseEntity<?> reservar(
            @PathVariable int id,
            @RequestHeader("X-Usuario-Id") int usuarioId,
            @RequestBody ReservarAssentosRequest req) {
        if (req.assentoIds() == null || req.assentoIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            List<AssentoId> ids = req.assentoIds().stream().map(AssentoId::new).toList();
            mapaServico.reservarAssentos(ids, new UsuarioId(usuarioId));
            List<Assento> assentos = mapaServico.listarAssentos(new MapaAssentosId(id));
            return ResponseEntity.ok(assentos.stream().map(AssentoResponse::fromDomain).toList());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/comprar")
    public ResponseEntity<?> comprar(
            @PathVariable int id,
            @RequestHeader("X-Usuario-Id") int usuarioId,
            @RequestBody ComprarAssentosRequest req) {
        if (req.assentoIds() == null || req.assentoIds().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("motivo", "assentoIds obrigatório"));
        }
        try {
            List<AssentoId> ids = req.assentoIds().stream().map(AssentoId::new).toList();
            Dinheiro total = compraServico.comprar(ids, new UsuarioId(usuarioId));
            return ResponseEntity.ok(new CompraAssentoResponse(total.getValor(),
                    "Compra realizada com sucesso!"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("motivo", e.getMessage()));
        }
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarVenda(
            @PathVariable int id,
            @RequestHeader("X-Usuario-Id") int usuarioId,
            @RequestBody ReservarAssentosRequest req) {
        if (req.assentoIds() == null || req.assentoIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            List<AssentoId> ids = req.assentoIds().stream().map(AssentoId::new).toList();
            mapaServico.confirmarVenda(ids, new UsuarioId(usuarioId));
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/bloquear")
    public ResponseEntity<?> bloquear(
            @PathVariable int id,
            @RequestHeader("X-Usuario-Id") int usuarioId,
            @RequestBody BloquearAssentosRequest req) {
        try {
            List<AssentoId> ids = req.assentoIds().stream().map(AssentoId::new).toList();
            mapaServico.bloquearAssentos(new MapaAssentosId(id), ids, new UsuarioId(usuarioId));
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        }
    }

    @PostMapping("/{id}/desbloquear")
    public ResponseEntity<?> desbloquear(
            @PathVariable int id,
            @RequestHeader("X-Usuario-Id") int usuarioId,
            @RequestBody BloquearAssentosRequest req) {
        try {
            List<AssentoId> ids = req.assentoIds().stream().map(AssentoId::new).toList();
            mapaServico.desbloquearAssentos(new MapaAssentosId(id), ids, new UsuarioId(usuarioId));
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        }
    }

    @GetMapping("/meus")
    public ResponseEntity<List<AssentoResponse>> meusAssentos(
            @RequestHeader("X-Usuario-Id") int usuarioId) {
        List<Assento> assentos = mapaServico.listarPorComprador(new UsuarioId(usuarioId));
        return ResponseEntity.ok(assentos.stream().map(AssentoResponse::fromDomain).toList());
    }

    @PostMapping("/{id}/liberar-expirados")
    public ResponseEntity<Void> liberarExpirados(@PathVariable int id) {
        mapaServico.liberarReservasExpiradas(new MapaAssentosId(id));
        return ResponseEntity.noContent().build();
    }
}
