package cesar.rv.ingressify.infraestrutura.memoria;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cesar.rv.ingressify.dominio.marketplace.feed.Comentario;
import cesar.rv.ingressify.dominio.marketplace.feed.ComentarioId;
import cesar.rv.ingressify.dominio.marketplace.feed.ComentarioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemId;

public class ComentarioRepositorioMemoria implements ComentarioRepositorio {

	private final Map<Integer, Comentario> store = new ConcurrentHashMap<>();
	private final AtomicInteger sequencia = new AtomicInteger(1);

	@Override
	public synchronized void salvar(Comentario comentario) {
		if (comentario.getId() == null) {
			comentario.atribuirId(new ComentarioId(sequencia.getAndIncrement()));
		}
		store.put(comentario.getId().getId(), comentario);
	}

	@Override
	public List<Comentario> pesquisarPorPostagem(PostagemId postagemId) {
		return store.values().stream()
				.filter(c -> c.getPostagemId().equals(postagemId))
				.toList();
	}

	public void limpar() {
		store.clear();
		sequencia.set(1);
	}
}
