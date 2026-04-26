package cesar.rv.ingressify.dominio.marketplace.compra;

import java.util.Optional;
import java.util.UUID;

public interface PedidoRepositorio {

	void salvar(Pedido pedido);

	Optional<Pedido> buscar(UUID id);

	Pedido obter(UUID id);

	void remover(UUID id);
}
