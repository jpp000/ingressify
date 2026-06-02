package cesar.rv.ingressify.infraestrutura.memoria;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.Denuncia;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.denuncia.StatusDenuncia;

public class DenunciaRepositorioMemoria implements DenunciaRepositorio {

	private final Map<Integer, Denuncia> store = new ConcurrentHashMap<>();
	private final AtomicInteger sequencia = new AtomicInteger(1);

	@Override
	public synchronized void salvar(Denuncia denuncia) {
		if (denuncia.getId() == null) {
			denuncia.atribuirId(new DenunciaId(sequencia.getAndIncrement()));
		}
		store.put(denuncia.getId().getId(), denuncia);
	}

	@Override
	public Denuncia obter(DenunciaId id) {
		Denuncia d = store.get(id.getId());
		if (d == null) {
			throw new IllegalArgumentException("Denuncia não encontrada: " + id.getId());
		}
		return d;
	}

	@Override
	public boolean existePorAnuncioEDenunciante(AnuncioRevendaId anuncioId, UsuarioId denuncianteId) {
		return store.values().stream()
				.anyMatch(d -> d.getAnuncioId().equals(anuncioId) && d.getDenuncianteId().equals(denuncianteId));
	}

	@Override
	public List<Denuncia> pesquisarPendentes() {
		return store.values().stream()
				.filter(d -> d.getStatus() == StatusDenuncia.PENDENTE)
				.toList();
	}

	public void limpar() {
		store.clear();
		sequencia.set(1);
	}
}
