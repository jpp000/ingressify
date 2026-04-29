package cesar.rv.ingressify.dominio.marketplace.feed;

import org.apache.commons.lang3.Validate;

public class FeedServico {

	private final PostagemRepositorio postagemRepositorio;
	private final ComentarioRepositorio comentarioRepositorio;

	public FeedServico(PostagemRepositorio postagemRepositorio, ComentarioRepositorio comentarioRepositorio) {
		Validate.notNull(postagemRepositorio, "postagemRepositorio");
		Validate.notNull(comentarioRepositorio, "comentarioRepositorio");
		this.postagemRepositorio = postagemRepositorio;
		this.comentarioRepositorio = comentarioRepositorio;
	}

	public void salvarPostagem(Postagem p) {
		postagemRepositorio.salvar(p);
	}

	public Postagem obterPostagem(PostagemId id) {
		return postagemRepositorio.obter(id);
	}

	public void removerPostagem(PostagemId id) {
		postagemRepositorio.remover(id);
	}

	public ComentarioId salvarComentario(Comentario c) {
		comentarioRepositorio.salvar(c);
		return java.util.Objects.requireNonNull(c.getId(), "id do comentário");
	}
}
