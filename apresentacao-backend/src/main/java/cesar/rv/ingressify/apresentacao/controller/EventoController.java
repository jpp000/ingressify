package cesar.rv.ingressify.apresentacao.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.CriarEventoRequest;
import cesar.rv.ingressify.apresentacao.dto.EditarEventoRequest;
import cesar.rv.ingressify.apresentacao.dto.EventoResponse;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

@RestController
@RequestMapping("/eventos")
public class EventoController {

	private final EventoServicoAplicacao eventoServico;

	public EventoController(EventoServicoAplicacao eventoServico) {
		this.eventoServico = eventoServico;
	}

	@PostMapping
	public ResponseEntity<EventoResponse> criar(
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody CriarEventoRequest req) {
		if (req.nome() == null || req.nome().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		if (req.dataHora() == null || req.dataHora().isBefore(LocalDateTime.now())) {
			return ResponseEntity.badRequest().build();
		}
		if (req.local() == null || req.local().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		if (req.capacidade() <= 0) {
			return ResponseEntity.badRequest().build();
		}
		try {
			EventoId id = eventoServico.criarEvento(
					new UsuarioId(usuarioId), req.nome(), req.dataHora(), req.local(),
					req.descricao(), req.capacidade(), req.imagemCapaUrl(),
					req.prazoReembolsoDias(), req.aberturaPortoes());
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(EventoResponse.fromDomain(eventoServico.obter(id)));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}

	@GetMapping
	public ResponseEntity<List<EventoResponse>> listar(
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		List<EventoResponse> lista = eventoServico
				.listarPorOrganizador(new UsuarioId(usuarioId))
				.stream()
				.map(EventoResponse::fromDomain)
				.toList();
		return ResponseEntity.ok(lista);
	}

	@GetMapping("/catalogo")
	public ResponseEntity<List<EventoResponse>> catalogo() {
		List<EventoResponse> lista = eventoServico
				.listarAtivos()
				.stream()
				.map(EventoResponse::fromDomain)
				.toList();
		return ResponseEntity.ok(lista);
	}

	@GetMapping("/{id}")
	public ResponseEntity<EventoResponse> detalhe(@PathVariable int id) {
		try {
			return ResponseEntity.ok(EventoResponse.fromDomain(eventoServico.obter(new EventoId(id))));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<EventoResponse> editar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody EditarEventoRequest req) {
		try {
			eventoServico.editarEvento(
					new EventoId(id), new UsuarioId(usuarioId), req.nome(), req.dataHora(),
					req.local(), req.descricao(), req.capacidade(), req.imagemCapaUrl(),
					req.prazoReembolsoDias(), req.aberturaPortoes());
			return ResponseEntity.ok(EventoResponse.fromDomain(eventoServico.obter(new EventoId(id))));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> remover(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			eventoServico.removerEvento(new EventoId(id), new UsuarioId(usuarioId));
			return ResponseEntity.noContent().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/{id}/cancelar")
	public ResponseEntity<EventoResponse> cancelar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			eventoServico.cancelarEvento(new EventoId(id), new UsuarioId(usuarioId));
			return ResponseEntity.ok(EventoResponse.fromDomain(eventoServico.obter(new EventoId(id))));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}
}
