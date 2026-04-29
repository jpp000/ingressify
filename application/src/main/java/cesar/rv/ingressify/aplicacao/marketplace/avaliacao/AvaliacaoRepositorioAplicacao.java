package cesar.rv.ingressify.aplicacao.marketplace.avaliacao;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface AvaliacaoRepositorioAplicacao {

	List<AvaliacaoResumo> pesquisarResumosPorEvento(EventoId eventoId);
}
