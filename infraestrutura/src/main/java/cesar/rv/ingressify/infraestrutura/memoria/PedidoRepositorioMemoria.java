package cesar.rv.ingressify.infraestrutura.memoria;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.compra.Pedido;
import cesar.rv.ingressify.dominio.marketplace.compra.PedidoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

public class PedidoRepositorioMemoria implements PedidoRepositorio {

	private final Map<UUID, Pedido> store = new ConcurrentHashMap<>();

	@Override
	public void salvar(Pedido pedido) {
		store.put(pedido.getId(), pedido);
	}

	@Override
	public Optional<Pedido> buscar(UUID id) {
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public Pedido obter(UUID id) {
		Pedido p = store.get(id);
		if (p == null) {
			throw new IllegalArgumentException("Pedido não encontrado: " + id);
		}
		return p;
	}

	@Override
	public void remover(UUID id) {
		store.remove(id);
	}

	@Override
	public List<Pedido> pesquisarPorCompradorETipoIngressoOrdenadoDesc(UsuarioId comprador,
			TipoIngressoId tipoIngressoId) {
		return store.values().stream()
				.filter(p -> p.getComprador().equals(comprador) && p.getTipoIngressoId().equals(tipoIngressoId))
				.sorted(Comparator.comparing(Pedido::getCriadaEm).reversed())
				.toList();
	}

	public void limpar() {
		store.clear();
	}
}
