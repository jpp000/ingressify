package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.compra.Pedido;
import cesar.rv.ingressify.dominio.marketplace.compra.PedidoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.PedidoJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.PedidoSpringDataRepository;

@Repository
public class PedidoRepositorioPersistencia implements PedidoRepositorio {

	private final PedidoSpringDataRepository jpa;

	public PedidoRepositorioPersistencia(PedidoSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(Pedido pedido) {
		jpa.save(PedidoJpa.fromDomain(pedido));
	}

	@Override
	public Optional<Pedido> buscar(UUID id) {
		return jpa.findById(id).map(PedidoJpa::toDomain);
	}

	@Override
	public Pedido obter(UUID id) {
		return jpa.findById(id)
				.map(PedidoJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));
	}

	@Override
	public void remover(UUID id) {
		jpa.deleteById(id);
	}

	@Override
	public List<Pedido> pesquisarPorCompradorETipoIngressoOrdenadoDesc(UsuarioId comprador, TipoIngressoId tipoIngressoId) {
		return jpa.findByCompradorIdAndTipoIngressoIdOrderByCriadaEmDesc(comprador.getId(), tipoIngressoId.getId())
				.stream()
				.map(PedidoJpa::toDomain)
				.toList();
	}
}
