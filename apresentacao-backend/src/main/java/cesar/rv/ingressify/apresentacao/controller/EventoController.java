package cesar.rv.ingressify.apresentacao.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.catalogo.CatalogoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.catalogo.FiltroCatalogo;
import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.CriarEventoRequest;
import cesar.rv.ingressify.apresentacao.dto.EditarEventoRequest;
import cesar.rv.ingressify.apresentacao.dto.EventoCatalogoResponse;
import cesar.rv.ingressify.apresentacao.dto.EventoResponse;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

@RestController
@RequestMapping("/eventos")
public class EventoController {

	private final EventoServicoAplicacao eventoServico;
	private final CatalogoServicoAplicacao catalogoServico;

	public EventoController(EventoServicoAplicacao eventoServico, CatalogoServicoAplicacao catalogoServico) {
		this.eventoServico = eventoServico;
		this.catalogoServico = catalogoServico;
	}

	@PostMapping
	public ResponseEntity<?> criar(
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody CriarEventoRequest req) {
		if (req.nome() == null || req.nome().isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("message", "Nome do evento é obrigatório."));
		}
		if (req.dataHora() == null || req.dataHora().isBefore(LocalDateTime.now())) {
			return ResponseEntity.badRequest().body(Map.of("message", "Data e hora do evento devem ser futuras."));
		}
		if (req.local() == null || req.local().isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("message", "Local do evento é obrigatório."));
		}
		if (req.capacidade() <= 0) {
			return ResponseEntity.badRequest().body(Map.of("message", "Capacidade do evento deve ser maior que zero."));
		}
		try {
			LocalDateTime aberturaPortoes = req.aberturaPortoes() != null ? req.aberturaPortoes() : req.dataHora();
			EventoId id = eventoServico.criarEvento(
					new UsuarioId(usuarioId), req.nome(), req.dataHora(), req.local(),
					req.descricao(), req.capacidade(), req.capacidadeNumerada(), req.imagemCapaUrl(),
					req.prazoReembolsoDias(), aberturaPortoes, req.categoria());
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(EventoResponse.fromDomain(eventoServico.obter(id)));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
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
	public ResponseEntity<List<EventoCatalogoResponse>> catalogo(
			@RequestParam(required = false) String nome,
			@RequestParam(required = false) String cidade,
			@RequestParam(required = false) String categoria,
			@RequestParam(required = false) BigDecimal precoMin,
			@RequestParam(required = false) BigDecimal precoMax,
			@RequestParam(required = false) LocalDateTime dataInicio,
			@RequestParam(required = false) LocalDateTime dataFim) {
		FiltroCatalogo filtro = new FiltroCatalogo();
		filtro.setNome(nome);
		filtro.setCidade(cidade);
		filtro.setCategoria(categoria);
		filtro.setPrecoMin(precoMin);
		filtro.setPrecoMax(precoMax);
		filtro.setDataInicio(dataInicio);
		filtro.setDataFim(dataFim);
		List<EventoCatalogoResponse> lista = catalogoServico.listar(filtro)
				.stream()
				.map(EventoCatalogoResponse::fromResumo)
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
			LocalDateTime aberturaPortoesEd = req.aberturaPortoes() != null ? req.aberturaPortoes() : req.dataHora();
			eventoServico.editarEvento(
					new EventoId(id), new UsuarioId(usuarioId), req.nome(), req.dataHora(),
					req.local(), req.descricao(), req.capacidade(), req.capacidadeNumerada(),
					req.imagemCapaUrl(), req.prazoReembolsoDias(), aberturaPortoesEd, req.categoria());
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
