package cesar.rv.ingressify.apresentacao.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.avaliacao.AvaliacaoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.AvaliacaoResponse;
import cesar.rv.ingressify.apresentacao.dto.CriarAvaliacaoRequest;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

@RestController
@RequestMapping("/eventos/{eventoId}/avaliacoes")
public class AvaliacaoController {

	private final AvaliacaoServicoAplicacao avaliacaoServico;

	public AvaliacaoController(AvaliacaoServicoAplicacao avaliacaoServico) {
		this.avaliacaoServico = avaliacaoServico;
	}

	@PostMapping
	public ResponseEntity<Void> avaliar(
			@PathVariable int eventoId,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody CriarAvaliacaoRequest req) {
		if (req.nota() < 1 || req.nota() > 5) {
			return ResponseEntity.badRequest().build();
		}
		try {
			avaliacaoServico.avaliar(new EventoId(eventoId), new UsuarioId(usuarioId),
					req.nota(), req.comentario());
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping
	public ResponseEntity<List<AvaliacaoResponse>> listar(@PathVariable int eventoId) {
		List<AvaliacaoResponse> resp = avaliacaoServico
				.listarPorEvento(new EventoId(eventoId))
				.stream()
				.map(AvaliacaoResponse::fromDomain)
				.toList();
		return ResponseEntity.ok(resp);
	}

	@PutMapping("/{avaliacaoId}/responder")
	public ResponseEntity<Void> responder(
			@PathVariable int eventoId,
			@PathVariable int avaliacaoId,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody String texto) {
		if (texto == null || texto.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		try {
			avaliacaoServico.responder(new AvaliacaoId(avaliacaoId), new UsuarioId(usuarioId), texto);
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}
}
