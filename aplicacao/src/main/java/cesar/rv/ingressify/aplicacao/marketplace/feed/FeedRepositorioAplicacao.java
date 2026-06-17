package cesar.rv.ingressify.aplicacao.marketplace.feed;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface FeedRepositorioAplicacao {

	List<PostagemResumo> pesquisarPorEvento(EventoId eventoId);
}
