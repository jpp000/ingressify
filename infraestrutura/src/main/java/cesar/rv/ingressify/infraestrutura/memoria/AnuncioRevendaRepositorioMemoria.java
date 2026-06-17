package cesar.rv.ingressify.infraestrutura.memoria;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

public class AnuncioRevendaRepositorioMemoria implements AnuncioRevendaRepositorio {

	private final Map<Integer, AnuncioRevenda> store = new ConcurrentHashMap<>();
	private final AtomicInteger sequencia = new AtomicInteger(1);

	@Override
	public synchronized void salvar(AnuncioRevenda anuncio) {
		if (anuncio.getId() == null) {
			anuncio.atribuirId(new AnuncioRevendaId(sequencia.getAndIncrement()));
		}
		store.put(anuncio.getId().getId(), anuncio);
	}

	@Override
	public AnuncioRevenda obter(AnuncioRevendaId id) {
		AnuncioRevenda a = store.get(id.getId());
		if (a == null) {
			throw new IllegalArgumentException("AnuncioRevenda não encontrado: " + id.getId());
		}
		return a;
	}

	@Override
	public void remover(AnuncioRevendaId id) {
		store.remove(id.getId());
	}

	@Override
	public boolean existeDisponivelOuReservadoParaIngresso(IngressoId ingressoId) {
		return store.values().stream()
				.filter(a -> a.getStatus() == StatusAnuncio.DISPONIVEL || a.getStatus() == StatusAnuncio.RESERVADO)
				.anyMatch(a -> a.getIngressoIds().contains(ingressoId));
	}

	@Override
	public boolean existeDisponivelOuReservadoParaEvento(EventoId eventoId) {
		return pesquisarPorEvento(eventoId).stream()
				.anyMatch(a -> a.getStatus() == StatusAnuncio.DISPONIVEL || a.getStatus() == StatusAnuncio.RESERVADO);
	}

	@Override
	public List<AnuncioRevenda> pesquisarPorEvento(EventoId eventoId) {
		return store.values().stream()
				.filter(a -> eventoId.equals(a.getEventoId())
						&& (a.getStatus() == StatusAnuncio.DISPONIVEL || a.getStatus() == StatusAnuncio.RESERVADO))
				.toList();
	}

	@Override
	public List<AnuncioRevenda> pesquisarPorVendedor(UsuarioId vendedorId) {
		return store.values().stream()
				.filter(a -> vendedorId.equals(a.getVendedor())
						&& (a.getStatus() == StatusAnuncio.DISPONIVEL || a.getStatus() == StatusAnuncio.RESERVADO))
				.toList();
	}

	@Override
	public List<AnuncioRevenda> listarTodos() {
		return store.values().stream()
				.filter(a -> a.getStatus() == StatusAnuncio.DISPONIVEL || a.getStatus() == StatusAnuncio.RESERVADO)
				.toList();
	}

	public void limpar() {
		store.clear();
		sequencia.set(1);
	}
}
