package cesar.rv.ingressify.infraestrutura.memoria;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckin;
import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckinRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public class RegistroCheckinRepositorioMemoria implements RegistroCheckinRepositorio {

	private final Map<UUID, RegistroCheckin> store = new ConcurrentHashMap<>();

	@Override
	public void salvar(RegistroCheckin registro) {
		store.put(registro.getId(), registro);
	}

	@Override
	public List<RegistroCheckin> pesquisarPorEvento(EventoId eventoId) {
		return store.values().stream()
				.filter(r -> r.getEventoId().equals(eventoId))
				.toList();
	}

	public void limpar() {
		store.clear();
	}
}
