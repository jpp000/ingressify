package cesar.rv.ingressify.dominio.marketplace.checkin;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface RegistroCheckinRepositorio {

	void salvar(RegistroCheckin registro);

	List<RegistroCheckin> pesquisarPorEvento(EventoId eventoId);
}
