package cesar.rv.ingressify.aplicacao.marketplace.ingresso;

import java.util.UUID;

import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;

public interface IngressoResumo {

	UUID getId();

	int getTipoIngressoId();

	int getEventoId();

	int getProprietarioId();

	StatusIngresso getStatus();
}
