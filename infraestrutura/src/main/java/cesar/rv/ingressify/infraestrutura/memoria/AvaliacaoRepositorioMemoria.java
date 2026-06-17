package cesar.rv.ingressify.infraestrutura.memoria;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.Avaliacao;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoId;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public class AvaliacaoRepositorioMemoria implements AvaliacaoRepositorio {

	private final Map<Integer, Avaliacao> store = new ConcurrentHashMap<>();
	private final AtomicInteger sequencia = new AtomicInteger(1);

	@Override
	public synchronized void salvar(Avaliacao avaliacao) {
		if (avaliacao.getId() == null) {
			avaliacao.atribuirId(new AvaliacaoId(sequencia.getAndIncrement()));
		}
		store.put(avaliacao.getId().getId(), avaliacao);
	}

	@Override
	public Avaliacao obter(AvaliacaoId id) {
		Avaliacao a = store.get(id.getId());
		if (a == null) {
			throw new IllegalArgumentException("Avaliacao não encontrada: " + id.getId());
		}
		return a;
	}

	@Override
	public Optional<Avaliacao> buscarPorEventoEUsuario(EventoId eventoId, UsuarioId usuarioId) {
		return store.values().stream()
				.filter(a -> a.getEventoId().equals(eventoId) && a.getUsuarioId().equals(usuarioId))
				.findFirst();
	}

	@Override
	public List<Avaliacao> pesquisarPorEvento(EventoId eventoId) {
		return store.values().stream()
				.filter(a -> a.getEventoId().equals(eventoId))
				.toList();
	}

	public void limpar() {
		store.clear();
		sequencia.set(1);
	}
}
