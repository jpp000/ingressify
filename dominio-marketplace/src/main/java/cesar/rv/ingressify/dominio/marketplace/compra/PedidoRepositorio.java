package cesar.rv.ingressify.dominio.marketplace.compra;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

public interface PedidoRepositorio {

	void salvar(Pedido pedido);

	Optional<Pedido> buscar(UUID id);

	Pedido obter(UUID id);

	void remover(UUID id);

	List<Pedido> pesquisarPorCompradorETipoIngressoOrdenadoDesc(UsuarioId comprador, TipoIngressoId tipoIngressoId);
}
