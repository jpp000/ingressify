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
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.denuncia.DenunciaServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.denuncia.DenunciaEventoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.DecidirDenunciaRequest;
import cesar.rv.ingressify.apresentacao.dto.DenunciaResponse;
import cesar.rv.ingressify.apresentacao.dto.DenunciarEventoRequest;
import cesar.rv.ingressify.apresentacao.dto.DenunciaEventoResponse;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DecisaoModeracao;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEventoId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.MotivoDenunciaEvento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

@RestController
@RequestMapping("/denuncias")
public class DenunciaController {

	private final DenunciaServicoAplicacao denunciaServico;
	private final DenunciaEventoServicoAplicacao denunciaEventoServico;

	public DenunciaController(DenunciaServicoAplicacao denunciaServico,
			DenunciaEventoServicoAplicacao denunciaEventoServico) {
		this.denunciaServico = denunciaServico;
		this.denunciaEventoServico = denunciaEventoServico;
	}

	// ── Denúncias de revendas (existentes) ──────────────────────────────────

	@GetMapping
	public ResponseEntity<?> listar(@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			List<DenunciaResponse> resp = denunciaServico.listar(new UsuarioId(usuarioId)).stream()
					.map(DenunciaResponse::fromDomain)
					.toList();
			return ResponseEntity.ok(resp);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("motivo", e.getMessage()));
		}
	}

	@PostMapping("/{id}/decidir")
	public ResponseEntity<?> decidir(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody DecidirDenunciaRequest req) {
		if (req.decisao() == null || req.decisao().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		try {
			denunciaServico.decidir(new DenunciaId(id), DecisaoModeracao.valueOf(req.decisao()),
					new UsuarioId(usuarioId));
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("motivo", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	// ── Denúncias de eventos (novo) ──────────────────────────────────────────

	@PostMapping("/eventos/{eventoId}")
	public ResponseEntity<?> denunciarEvento(
			@PathVariable int eventoId,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody DenunciarEventoRequest req) {
		if (req.motivo() == null || req.motivo().isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("motivo", "motivo é obrigatório"));
		}
		try {
			MotivoDenunciaEvento motivo = MotivoDenunciaEvento.valueOf(req.motivo());
			denunciaEventoServico.denunciar(new EventoId(eventoId), new UsuarioId(usuarioId),
					motivo, req.descricao());
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("motivo", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("motivo", "motivo inválido"));
		}
	}

	@GetMapping("/eventos")
	public ResponseEntity<?> listarEventos(@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			List<DenunciaEventoResponse> resp = denunciaEventoServico.listar(new UsuarioId(usuarioId))
					.stream().map(DenunciaEventoResponse::fromDomain).toList();
			return ResponseEntity.ok(resp);
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("motivo", e.getMessage()));
		}
	}

	@PostMapping("/eventos/{id}/analisar")
	public ResponseEntity<?> analisarDenunciaEvento(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			denunciaEventoServico.iniciarAnalise(new DenunciaEventoId(id), new UsuarioId(usuarioId));
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("motivo", e.getMessage()));
		}
	}

	@PostMapping("/eventos/{id}/aprovar")
	public ResponseEntity<?> aprovarDenunciaEvento(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			denunciaEventoServico.aprovar(new DenunciaEventoId(id), new UsuarioId(usuarioId));
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("motivo", e.getMessage()));
		}
	}

	@PostMapping("/eventos/{id}/rejeitar")
	public ResponseEntity<?> rejeitarDenunciaEvento(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			denunciaEventoServico.rejeitar(new DenunciaEventoId(id), new UsuarioId(usuarioId));
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("motivo", e.getMessage()));
		}
	}
}
