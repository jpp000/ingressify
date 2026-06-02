package cesar.rv.ingressify.infraestrutura.memoria;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.feed.Postagem;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemId;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemRepositorio;

public class PostagemRepositorioMemoria implements PostagemRepositorio {

	private final Map<Integer, Postagem> store = new ConcurrentHashMap<>();
	private final AtomicInteger sequencia = new AtomicInteger(1);

	@Override
	public synchronized void salvar(Postagem postagem) {
		if (postagem.getId() == null) {
			postagem.atribuirId(new PostagemId(sequencia.getAndIncrement()));
		}
		store.put(postagem.getId().getId(), postagem);
	}

	@Override
	public Postagem obter(PostagemId id) {
		Postagem p = store.get(id.getId());
		if (p == null) {
			throw new IllegalArgumentException("Postagem não encontrada: " + id.getId());
		}
		return p;
	}

	@Override
	public void remover(PostagemId id) {
		store.remove(id.getId());
	}

	@Override
	public List<Postagem> pesquisarPorEvento(EventoId eventoId) {
		return store.values().stream()
				.filter(p -> p.getEventoId().equals(eventoId))
				.toList();
	}

	@Override
	public Optional<Postagem> buscarFixadaPorEvento(EventoId eventoId) {
		return store.values().stream()
				.filter(p -> p.getEventoId().equals(eventoId) && p.isFixada())
				.findFirst();
	}

	public void limpar() {
		store.clear();
		sequencia.set(1);
	}
}
