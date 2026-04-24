package cesar.rv.ingressify.aplicacao.marketplace.anuncio;

import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoResumo;
import cesar.rv.ingressify.aplicacao.marketplace.ingresso.IngressoResumo;

public interface AnuncioResumoExpandido extends AnuncioResumo {

	IngressoResumo getIngresso();

	EventoResumo getEvento();
}
