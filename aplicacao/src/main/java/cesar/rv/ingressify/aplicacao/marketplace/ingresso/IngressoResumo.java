package cesar.rv.ingressify.aplicacao.marketplace.ingresso;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;

public interface IngressoResumo {

	IngressoId getId();

	EventoId getEventoId();

	String getEventoNome();

	String getTipoNome();

	StatusIngresso getStatus();

	boolean isBloqueadoPorReembolso();
}
