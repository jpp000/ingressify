package cesar.rv.ingressify.aplicacao.marketplace.ingresso;

import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoResumo;
import cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso.TipoIngressoResumo;

public interface IngressoResumoExpandido extends IngressoResumo {

	EventoResumo getEvento();

	TipoIngressoResumo getTipoIngresso();
}
