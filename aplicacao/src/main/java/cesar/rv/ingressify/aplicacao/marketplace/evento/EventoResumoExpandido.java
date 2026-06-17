package cesar.rv.ingressify.aplicacao.marketplace.evento;

import java.util.List;

import cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso.TipoIngressoResumo;

public interface EventoResumoExpandido extends EventoResumo {

	List<TipoIngressoResumo> getTipos();

	double getMediaAvaliacao();
}
