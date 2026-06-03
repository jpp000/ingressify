package cesar.rv.ingressify.apresentacao.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

import cesar.rv.ingressify.aplicacao.marketplace.anuncioRevenda.AnuncioRevendaServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.denuncia.DenunciaServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.AnuncioRevendaResponse;
import cesar.rv.ingressify.apresentacao.dto.CriarAnuncioRequest;
import cesar.rv.ingressify.apresentacao.dto.DenunciaRequest;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.MotivoDenuncia;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

@RestController
@RequestMapping("/revendas/anuncios")
public class RevendaController {

	private final AnuncioRevendaServicoAplicacao anuncioServico;
	private final DenunciaServicoAplicacao denunciaServico;

	public RevendaController(AnuncioRevendaServicoAplicacao anuncioServico,
			DenunciaServicoAplicacao denunciaServico) {
		this.anuncioServico = anuncioServico;
		this.denunciaServico = denunciaServico;
	}

	@PostMapping
	public ResponseEntity<?> criar(
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody CriarAnuncioRequest req) {
		if (req.ingressoIds() == null || req.ingressoIds().isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		if (req.preco() == null || req.preco().signum() <= 0) {
			return ResponseEntity.badRequest().build();
		}
		try {
			List<IngressoId> ids = req.ingressoIds().stream()
					.map(s -> new IngressoId(UUID.fromString(s))).toList();
			AnuncioRevendaId anuncioId = anuncioServico.anunciar(
					new UsuarioId(usuarioId), ids, req.preco());
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(AnuncioRevendaResponse.fromDomain(anuncioServico.obter(anuncioId)));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("motivo", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/meus")
	public ResponseEntity<List<AnuncioRevendaResponse>> meus(
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		List<AnuncioRevendaResponse> resp = anuncioServico
				.listarPorVendedor(new UsuarioId(usuarioId))
				.stream()
				.map(AnuncioRevendaResponse::fromDomain)
				.toList();
		return ResponseEntity.ok(resp);
	}

	@GetMapping
	public ResponseEntity<List<AnuncioRevendaResponse>> listar(
			@RequestParam(required = false) Integer eventoId) {
		List<AnuncioRevenda> anuncios = eventoId != null
				? anuncioServico.listarPorEvento(new EventoId(eventoId))
				: anuncioServico.listarTodos();
		return ResponseEntity.ok(anuncios.stream().map(AnuncioRevendaResponse::fromDomain).toList());
	}

	@GetMapping("/{id}")
	public ResponseEntity<AnuncioRevendaResponse> detalhe(@PathVariable int id) {
		try {
			return ResponseEntity.ok(
					AnuncioRevendaResponse.fromDomain(anuncioServico.obter(new AnuncioRevendaId(id))));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<AnuncioRevendaResponse> editar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody BigDecimal novoPreco) {
		if (novoPreco == null || novoPreco.signum() <= 0) {
			return ResponseEntity.badRequest().build();
		}
		try {
			anuncioServico.alterarPreco(new AnuncioRevendaId(id), new UsuarioId(usuarioId), novoPreco);
			return ResponseEntity.ok(
					AnuncioRevendaResponse.fromDomain(anuncioServico.obter(new AnuncioRevendaId(id))));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> cancelar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			anuncioServico.cancelar(new AnuncioRevendaId(id), new UsuarioId(usuarioId));
			return ResponseEntity.noContent().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/{id}/reservar")
	public ResponseEntity<?> reservar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			anuncioServico.reservar(new AnuncioRevendaId(id), new UsuarioId(usuarioId));
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(AnuncioRevendaResponse.fromDomain(anuncioServico.obter(new AnuncioRevendaId(id))));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("motivo", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/{id}/confirmar")
	public ResponseEntity<?> confirmar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			AnuncioRevenda anuncio = anuncioServico.obter(new AnuncioRevendaId(id));
			if (anuncio.getCompradorReservado() == null
					|| anuncio.getCompradorReservado().getId() != usuarioId) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
			anuncioServico.confirmarCompra(new AnuncioRevendaId(id));
			return ResponseEntity.ok(AnuncioRevendaResponse.fromDomain(anuncioServico.obter(new AnuncioRevendaId(id))));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("motivo", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/{id}/denunciar")
	public ResponseEntity<Void> denunciar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody DenunciaRequest req) {
		if (req.motivo() == null || req.motivo().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		try {
			denunciaServico.denunciar(new AnuncioRevendaId(id), new UsuarioId(usuarioId),
					MotivoDenuncia.valueOf(req.motivo()), req.descricao());
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}
}
