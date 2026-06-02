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
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;

@RestController
@RequestMapping("/eventos")
public class EventoController {

	private final EventoServicoAplicacao eventoServico;
	private final EventoRepositorio eventoRepositorio;

	public EventoController(EventoServicoAplicacao eventoServico, EventoRepositorio eventoRepositorio) {
		this.eventoServico = eventoServico;
		this.eventoRepositorio = eventoRepositorio;
	}

	@PostMapping
	public ResponseEntity<EventoResponse> criar(
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody CriarEventoRequest req) {
		if (req.dataHora() == null || req.dataHora().isBefore(LocalDateTime.now())) {
			return ResponseEntity.badRequest().build();
		}
		if (req.capacidade() <= 0) {
			return ResponseEntity.badRequest().build();
		}
		EventoId id = eventoServico.criarEvento(
				new UsuarioId(usuarioId), req.nome(), req.dataHora(), req.local(),
				req.descricao(), req.capacidade(), req.imagemCapaUrl(),
				req.prazoReembolsoDias(), req.aberturaPortoes());
		Evento evento = eventoRepositorio.obter(id);
		return ResponseEntity.status(HttpStatus.CREATED).body(EventoResponse.fromDomain(evento));
	}

	@GetMapping
	public ResponseEntity<List<EventoResponse>> listar(
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		List<EventoResponse> lista = eventoRepositorio
				.listarPorOrganizador(new UsuarioId(usuarioId))
				.stream()
				.map(EventoResponse::fromDomain)
				.toList();
		return ResponseEntity.ok(lista);
	}

	@GetMapping("/{id}")
	public ResponseEntity<EventoResponse> detalhe(@PathVariable int id) {
		try {
			Evento evento = eventoRepositorio.obter(new EventoId(id));
			return ResponseEntity.ok(EventoResponse.fromDomain(evento));
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
			Evento evento = eventoRepositorio.obter(new EventoId(id));
			return ResponseEntity.ok(EventoResponse.fromDomain(evento));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
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
		}
	}

	@PostMapping("/{id}/cancelar")
	public ResponseEntity<EventoResponse> cancelar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			eventoServico.cancelarEvento(new EventoId(id), new UsuarioId(usuarioId));
			Evento evento = eventoRepositorio.obter(new EventoId(id));
			return ResponseEntity.ok(EventoResponse.fromDomain(evento));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}
}
