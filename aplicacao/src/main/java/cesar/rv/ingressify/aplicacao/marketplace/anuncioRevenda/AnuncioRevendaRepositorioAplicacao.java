package cesar.rv.ingressify.aplicacao.marketplace.anuncioRevenda;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface AnuncioRevendaRepositorioAplicacao {

	List<AnuncioRevendaResumo> pesquisarResumosPorEvento(EventoId eventoId);

	List<AnuncioRevendaResumo> pesquisarDisponiveisPorEvento(EventoId eventoId);
}
