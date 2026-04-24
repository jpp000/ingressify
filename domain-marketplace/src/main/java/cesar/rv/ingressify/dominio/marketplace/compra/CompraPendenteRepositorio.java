package cesar.rv.ingressify.dominio.marketplace.compra;

import java.util.Optional;
import java.util.UUID;

public interface CompraPendenteRepositorio {

	void salvar(CompraPendente compra);

	Optional<CompraPendente> buscar(UUID id);

	CompraPendente obter(UUID id);

	void remover(UUID id);
}
