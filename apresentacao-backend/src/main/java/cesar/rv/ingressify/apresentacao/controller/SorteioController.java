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

import cesar.rv.ingressify.aplicacao.marketplace.sorteio.SorteioServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.CriarSorteioRequest;
import cesar.rv.ingressify.apresentacao.dto.InscricaoSorteioResponse;
import cesar.rv.ingressify.apresentacao.dto.SorteioResponse;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

@RestController
@RequestMapping("/sorteios")
public class SorteioController {

    private final SorteioServicoAplicacao sorteioServico;

    public SorteioController(SorteioServicoAplicacao sorteioServico) {
        this.sorteioServico = sorteioServico;
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @RequestHeader("X-Usuario-Id") int usuarioId,
            @RequestBody CriarSorteioRequest req) {
        if (req.eventoId() == null || req.tipoIngressoId() == null
                || req.quantidadeIngressos() == null || req.quantidadeIngressos() <= 0
                || req.prazoInscricao() == null || req.prazoConfirmacaoHoras() == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            SorteioId id = sorteioServico.criar(
                    new EventoId(req.eventoId()),
                    new TipoIngressoId(req.tipoIngressoId()),
                    new UsuarioId(usuarioId),
                    req.quantidadeIngressos(),
                    req.quantidadeListaEspera() != null ? req.quantidadeListaEspera() : 0,
                    req.prazoInscricao(),
                    req.prazoConfirmacaoHoras());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(SorteioResponse.fromDomain(sorteioServico.obter(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("motivo", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<SorteioResponse>> listar(
            @RequestParam Integer eventoId) {
        return ResponseEntity.ok(
                sorteioServico.listarPorEvento(new EventoId(eventoId)).stream()
                        .map(SorteioResponse::fromDomain)
                        .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SorteioResponse> detalhe(@PathVariable int id) {
        try {
            return ResponseEntity.ok(SorteioResponse.fromDomain(sorteioServico.obter(new SorteioId(id))));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/abrir")
    public ResponseEntity<?> abrirInscricoes(
            @PathVariable int id,
            @RequestHeader("X-Usuario-Id") int usuarioId) {
        try {
            sorteioServico.abrirInscricoes(new SorteioId(id), new UsuarioId(usuarioId));
            return ResponseEntity.ok(SorteioResponse.fromDomain(sorteioServico.obter(new SorteioId(id))));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/encerrar-inscricoes")
    public ResponseEntity<?> encerrarInscricoes(
            @PathVariable int id,
            @RequestHeader("X-Usuario-Id") int usuarioId) {
        try {
            sorteioServico.encerrarInscricoes(new SorteioId(id), new UsuarioId(usuarioId));
            return ResponseEntity.ok(SorteioResponse.fromDomain(sorteioServico.obter(new SorteioId(id))));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/inscrever")
    public ResponseEntity<?> inscrever(
            @PathVariable int id,
            @RequestHeader("X-Usuario-Id") int usuarioId) {
        try {
            sorteioServico.inscrever(new SorteioId(id), new UsuarioId(usuarioId));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(SorteioResponse.fromDomain(sorteioServico.obter(new SorteioId(id))));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/sortear")
    public ResponseEntity<?> executarSorteio(
            @PathVariable int id,
            @RequestHeader("X-Usuario-Id") int usuarioId) {
        try {
            sorteioServico.executarSorteio(new SorteioId(id));
            return ResponseEntity.ok(SorteioResponse.fromDomain(sorteioServico.obter(new SorteioId(id))));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarParticipacao(
            @PathVariable int id,
            @RequestHeader("X-Usuario-Id") int usuarioId) {
        try {
            sorteioServico.confirmarParticipacao(new SorteioId(id), new UsuarioId(usuarioId));
            return ResponseEntity.ok(SorteioResponse.fromDomain(sorteioServico.obter(new SorteioId(id))));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(
            @PathVariable int id,
            @RequestHeader("X-Usuario-Id") int usuarioId) {
        try {
            sorteioServico.cancelar(new SorteioId(id), new UsuarioId(usuarioId));
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/inscricoes")
    public ResponseEntity<List<InscricaoSorteioResponse>> inscricoes(@PathVariable int id) {
        try {
            return ResponseEntity.ok(
                    sorteioServico.listarInscricoes(new SorteioId(id)).stream()
                            .map(InscricaoSorteioResponse::fromDomain)
                            .toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
