package cesar.rv.ingressify.dominio.marketplace.feed;

import java.util.List;
import java.util.Optional;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface PostagemRepositorio {

	Postagem obter(PostagemId id);

	void salvar(Postagem postagem);

	void remover(PostagemId id);

	List<Postagem> pesquisarPorEvento(EventoId eventoId);

	Optional<Postagem> buscarFixadaPorEvento(EventoId eventoId);
}
