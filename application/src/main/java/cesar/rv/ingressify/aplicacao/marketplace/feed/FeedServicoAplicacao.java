package cesar.rv.ingressify.aplicacao.marketplace.feed;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.Papel;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;
import cesar.rv.ingressify.dominio.marketplace.feed.Comentario;
import cesar.rv.ingressify.dominio.marketplace.feed.ComentarioId;
import cesar.rv.ingressify.dominio.marketplace.feed.FeedServico;
import cesar.rv.ingressify.dominio.marketplace.feed.Postagem;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemId;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;

public class FeedServicoAplicacao {

	private final FeedServico feedServico;
	private final EventoRepositorio eventoRepositorio;
	private final IngressoRepositorio ingressoRepositorio;
	private final UsuarioRepositorio usuarioRepositorio;
	private final PostagemRepositorio postagemRepositorio;

	public FeedServicoAplicacao(FeedServico feedServico, EventoRepositorio eventoRepositorio,
			IngressoRepositorio ingressoRepositorio, UsuarioRepositorio usuarioRepositorio,
			PostagemRepositorio postagemRepositorio) {
		Validate.notNull(feedServico, "feedServico");
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		Validate.notNull(ingressoRepositorio, "ingressoRepositorio");
		Validate.notNull(usuarioRepositorio, "usuarioRepositorio");
		Validate.notNull(postagemRepositorio, "postagemRepositorio");
		this.feedServico = feedServico;
		this.eventoRepositorio = eventoRepositorio;
		this.ingressoRepositorio = ingressoRepositorio;
		this.usuarioRepositorio = usuarioRepositorio;
		this.postagemRepositorio = postagemRepositorio;
	}

	public PostagemId publicar(EventoId eventoId, UsuarioId autorId, String titulo, String conteudo, String imagemUrl,
			boolean fixar) {
		Evento evento = eventoRepositorio.obter(eventoId);
		if (!evento.getOrganizadorId().equals(autorId)) {
			throw new IllegalStateException("apenas o organizador pode publicar");
		}
		if (evento.getStatus() == StatusEvento.CANCELADO) {
			throw new IllegalStateException("evento cancelado");
		}
		LocalDateTime agora = LocalDateTime.now();
		if (fixar) {
			postagemRepositorio.buscarFixadaPorEvento(eventoId).ifPresent(p -> {
				p.desfixar();
				feedServico.salvarPostagem(p);
			});
		}
		Postagem postagem = new Postagem(eventoId, autorId, titulo, conteudo, imagemUrl, fixar, agora);
		feedServico.salvarPostagem(postagem);
		return postagem.getId();
	}

	public void editar(PostagemId postagemId, UsuarioId autorId, String titulo, String conteudo, String imagemUrl) {
		Postagem p = feedServico.obterPostagem(postagemId);
		if (!p.getAutorId().equals(autorId)) {
			throw new IllegalStateException("apenas o autor pode editar");
		}
		p.editar(titulo, conteudo, imagemUrl);
		feedServico.salvarPostagem(p);
	}

	public void remover(PostagemId postagemId, UsuarioId autorId) {
		Postagem p = feedServico.obterPostagem(postagemId);
		if (!p.getAutorId().equals(autorId)) {
			throw new IllegalStateException("apenas o autor pode remover");
		}
		feedServico.removerPostagem(postagemId);
	}

	public void fixar(PostagemId postagemId, UsuarioId autorId) {
		Postagem p = feedServico.obterPostagem(postagemId);
		if (!p.getAutorId().equals(autorId)) {
			throw new IllegalStateException("apenas o autor pode fixar");
		}
		if (!usuarioRepositorio.obter(autorId).temPapel(Papel.ORGANIZADOR)) {
			throw new IllegalStateException("apenas organizador pode fixar postagem");
		}
		postagemRepositorio.buscarFixadaPorEvento(p.getEventoId()).ifPresent(outra -> {
			if (!outra.getId().equals(p.getId())) {
				outra.desfixar();
				feedServico.salvarPostagem(outra);
			}
		});
		p.fixar();
		feedServico.salvarPostagem(p);
	}

	public void desfixar(PostagemId postagemId, UsuarioId autorId) {
		Postagem p = feedServico.obterPostagem(postagemId);
		if (!p.getAutorId().equals(autorId)) {
			throw new IllegalStateException("apenas o autor pode desfixar");
		}
		p.desfixar();
		feedServico.salvarPostagem(p);
	}

	public ComentarioId comentar(PostagemId postagemId, UsuarioId usuarioId, String texto) {
		Postagem postagem = feedServico.obterPostagem(postagemId);
		boolean temIngresso = ingressoRepositorio.pesquisarPorEvento(postagem.getEventoId()).stream()
				.anyMatch(i -> i.getProprietario().equals(usuarioId));
		if (!temIngresso) {
			throw new IllegalStateException("usuário precisa de ingresso no evento para comentar");
		}
		return feedServico.salvarComentario(new Comentario(postagemId, usuarioId, texto, LocalDateTime.now()));
	}
}
