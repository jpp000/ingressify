package cesar.rv.ingressify.apresentacao.controller;

import java.util.Comparator;
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

import cesar.rv.ingressify.aplicacao.marketplace.feed.FeedServicoAplicacao;
import cesar.rv.ingressify.apresentacao.dto.CriarPostagemRequest;
import cesar.rv.ingressify.apresentacao.dto.PostagemResponse;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemId;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemRepositorio;

@RestController
@RequestMapping("/eventos/{eventoId}/posts")
public class FeedEventoController {

	private final FeedServicoAplicacao feedServico;
	private final PostagemRepositorio postagemRepositorio;

	public FeedEventoController(FeedServicoAplicacao feedServico, PostagemRepositorio postagemRepositorio) {
		this.feedServico = feedServico;
		this.postagemRepositorio = postagemRepositorio;
	}

	@PostMapping
	public ResponseEntity<Void> criar(
			@PathVariable int eventoId,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody CriarPostagemRequest req) {
		try {
			feedServico.publicar(new EventoId(eventoId), new UsuarioId(usuarioId),
					req.titulo(), req.conteudo(), req.imagemUrl(), req.fixar());
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}

	@GetMapping
	public ResponseEntity<List<PostagemResponse>> listar(@PathVariable int eventoId) {
		List<PostagemResponse> resp = postagemRepositorio
				.pesquisarPorEvento(new EventoId(eventoId))
				.stream()
				.sorted(Comparator.comparing(
						p -> Boolean.FALSE.equals(p.isFixada())))
				.map(PostagemResponse::fromDomain)
				.toList();
		return ResponseEntity.ok(resp);
	}

	@PutMapping("/{postId}")
	public ResponseEntity<Void> editar(
			@PathVariable int eventoId,
			@PathVariable int postId,
			@RequestHeader("X-Usuario-Id") int usuarioId,
			@RequestBody CriarPostagemRequest req) {
		try {
			feedServico.editar(new PostagemId(postId), new UsuarioId(usuarioId),
					req.titulo(), req.conteudo(), req.imagemUrl());
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}

	@DeleteMapping("/{postId}")
	public ResponseEntity<Void> remover(
			@PathVariable int eventoId,
			@PathVariable int postId,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			feedServico.remover(new PostagemId(postId), new UsuarioId(usuarioId));
			return ResponseEntity.noContent().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}

	@PostMapping("/{postId}/fixar")
	public ResponseEntity<Void> fixar(
			@PathVariable int eventoId,
			@PathVariable int postId,
			@RequestHeader("X-Usuario-Id") int usuarioId) {
		try {
			feedServico.fixar(new PostagemId(postId), new UsuarioId(usuarioId));
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}
}
