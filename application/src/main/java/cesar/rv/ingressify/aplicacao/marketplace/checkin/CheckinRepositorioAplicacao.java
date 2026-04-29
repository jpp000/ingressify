package cesar.rv.ingressify.aplicacao.marketplace.checkin;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface CheckinRepositorioAplicacao {

	List<CheckinResumo> pesquisarPorEvento(EventoId eventoId);
}
