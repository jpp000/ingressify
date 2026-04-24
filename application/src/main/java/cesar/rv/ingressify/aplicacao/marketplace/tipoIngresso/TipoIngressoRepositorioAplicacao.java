package cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

public interface TipoIngressoRepositorioAplicacao {

	List<TipoIngressoResumo> pesquisarResumosPorEvento(EventoId eventoId);

	List<TipoIngressoResumo> pesquisarResumos();

	TipoIngressoResumo obterResumo(TipoIngressoId id);
}
