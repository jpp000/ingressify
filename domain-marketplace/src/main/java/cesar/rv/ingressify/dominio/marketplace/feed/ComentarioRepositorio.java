package cesar.rv.ingressify.dominio.marketplace.feed;

import java.util.List;

public interface ComentarioRepositorio {

	void salvar(Comentario comentario);

	List<Comentario> pesquisarPorPostagem(PostagemId postagemId);
}
