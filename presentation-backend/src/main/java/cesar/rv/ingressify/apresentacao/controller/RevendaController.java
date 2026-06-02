package cesar.rv.ingressify.apresentacao.controller;

import java.math.BigDecimal;
import java.util.List;
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
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaServico;
import cesar.rv.ingressify.dominio.marketplace.denuncia.MotivoDenuncia;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

@RestController
@RequestMapping("/revendas/anuncios")
public class RevendaController {

	private final AnuncioRevendaServicoAplicacao anuncioServico;
	private final AnuncioRevendaServico anuncioRevendaServico;
	private final AnuncioRevendaRepositorio anuncioRepositorio;
	private final DenunciaServicoAplicacao denunciaServico;

	public RevendaController(AnuncioRevendaServicoAplicacao anuncioServico,
			AnuncioRevendaServico anuncioRevendaServico, AnuncioRevendaRepositorio anuncioRepositorio,
			DenunciaServicoAplicacao denunciaServico) {
		this.anuncioServico = anuncioServico;
		this.anuncioRevendaServico = anuncioRevendaServico;
		this.anuncioRepositorio = anuncioRepositorio;
		this.denunciaServico = denunciaServico;
	}

	@PostMapping
	public ResponseEntity<AnuncioRevendaResponse> criar(
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody CriarAnuncioRequest req) {
		if (req.ingressoIds() == null || req.ingressoIds().isEmpty() || req.preco() == null) {
			return ResponseEntity.badRequest().build();
		}
		try {
			List<IngressoId> ids = req.ingressoIds().stream()
					.map(s -> new IngressoId(UUID.fromString(s))).toList();
			AnuncioRevendaId anuncioId = anuncioServico.anunciar(
					new UsuarioId(usuarioId), ids, req.preco());
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(AnuncioRevendaResponse.fromDomain(anuncioRevendaServico.obter(anuncioId)));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}

	@GetMapping
	public ResponseEntity<List<AnuncioRevendaResponse>> listar(
			@RequestParam(required = false) Integer eventoId) {
		if (eventoId == null) {
			return ResponseEntity.badRequest().build();
		}
		List<AnuncioRevendaResponse> resp = anuncioRepositorio
				.pesquisarPorEvento(new EventoId(eventoId))
				.stream()
				.map(AnuncioRevendaResponse::fromDomain)
				.toList();
		return ResponseEntity.ok(resp);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AnuncioRevendaResponse> detalhe(@PathVariable int id) {
		try {
			return ResponseEntity.ok(
					AnuncioRevendaResponse.fromDomain(anuncioRevendaServico.obter(new AnuncioRevendaId(id))));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<AnuncioRevendaResponse> editar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody BigDecimal novoPreco) {
		try {
			anuncioServico.alterarPreco(new AnuncioRevendaId(id), new UsuarioId(usuarioId), novoPreco);
			return ResponseEntity.ok(
					AnuncioRevendaResponse.fromDomain(anuncioRevendaServico.obter(new AnuncioRevendaId(id))));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
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
		}
	}

	@PostMapping("/{id}/reservar")
	public ResponseEntity<AnuncioRevendaResponse> reservar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			anuncioServico.reservar(new AnuncioRevendaId(id), new UsuarioId(usuarioId));
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(AnuncioRevendaResponse.fromDomain(anuncioRevendaServico.obter(new AnuncioRevendaId(id))));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}

	@PostMapping("/{id}/denunciar")
	public ResponseEntity<Void> denunciar(
			@PathVariable int id,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody DenunciaRequest req) {
		try {
			denunciaServico.denunciar(new AnuncioRevendaId(id), new UsuarioId(usuarioId),
					MotivoDenuncia.valueOf(req.motivo()), req.descricao());
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}
}
