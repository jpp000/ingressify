package cesar.rv.ingressify.apresentacao.controller;

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

import cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso.TipoIngressoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.CriarTipoIngressoRequest;
import cesar.rv.ingressify.apresentacao.dto.EditarTipoIngressoRequest;
import cesar.rv.ingressify.apresentacao.dto.TipoIngressoResponse;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;

@RestController
@RequestMapping("/eventos/{eventoId}/tipos-ingresso")
public class TipoIngressoController {

	private final TipoIngressoServicoAplicacao tipoIngressoServico;
	private final TipoIngressoRepositorio tipoIngressoRepositorio;

	public TipoIngressoController(TipoIngressoServicoAplicacao tipoIngressoServico,
			TipoIngressoRepositorio tipoIngressoRepositorio) {
		this.tipoIngressoServico = tipoIngressoServico;
		this.tipoIngressoRepositorio = tipoIngressoRepositorio;
	}

	@PostMapping
	public ResponseEntity<TipoIngressoResponse> criar(
			@PathVariable int eventoId,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody CriarTipoIngressoRequest req) {
		if (req.preco() == null || req.preco().signum() <= 0) {
			return ResponseEntity.badRequest().build();
		}
		if (req.quantidade() <= 0) {
			return ResponseEntity.badRequest().build();
		}
		try {
			TipoIngressoId id = tipoIngressoServico.criarTipoIngresso(
					new EventoId(eventoId), new UsuarioId(usuarioId),
					req.nome(), req.preco(), req.quantidade(), req.descricao());
			var tipo = tipoIngressoRepositorio.obter(id);
			return ResponseEntity.status(HttpStatus.CREATED).body(TipoIngressoResponse.fromDomain(tipo));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}

	@GetMapping
	public ResponseEntity<List<TipoIngressoResponse>> listar(@PathVariable int eventoId) {
		List<TipoIngressoResponse> lista = tipoIngressoRepositorio
				.pesquisarPorEvento(new EventoId(eventoId))
				.stream()
				.map(TipoIngressoResponse::fromDomain)
				.toList();
		return ResponseEntity.ok(lista);
	}

	@PutMapping("/{tipoId}")
	public ResponseEntity<TipoIngressoResponse> editar(
			@PathVariable int eventoId,
			@PathVariable int tipoId,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody EditarTipoIngressoRequest req) {
		try {
			tipoIngressoServico.editarTipoIngresso(
					new TipoIngressoId(tipoId), new UsuarioId(usuarioId),
					req.nome(), req.preco(), req.quantidade(), req.descricao());
			var tipo = tipoIngressoRepositorio.obter(new TipoIngressoId(tipoId));
			return ResponseEntity.ok(TipoIngressoResponse.fromDomain(tipo));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}

	@DeleteMapping("/{tipoId}")
	public ResponseEntity<Void> remover(
			@PathVariable int eventoId,
			@PathVariable int tipoId,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			tipoIngressoServico.removerTipoIngresso(new TipoIngressoId(tipoId), new UsuarioId(usuarioId));
			return ResponseEntity.noContent().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}
}
