package cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface TipoIngressoRepositorioAplicacao {

	List<TipoIngressoResumo> pesquisarResumosPorEvento(EventoId eventoId);
}
