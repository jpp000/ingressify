package cesar.rv.ingressify.dominio.marketplace.cupom;

import java.util.List;
import java.util.Optional;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface CupomRepositorio {

	void salvar(Cupom cupom);

	Cupom obter(CupomId id);

	Optional<Cupom> buscarPorCodigo(String codigo);

	List<Cupom> pesquisarPorEvento(EventoId eventoId);
}
