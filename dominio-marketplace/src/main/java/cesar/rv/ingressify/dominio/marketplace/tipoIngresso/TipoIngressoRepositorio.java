package cesar.rv.ingressify.dominio.marketplace.tipoIngresso;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface TipoIngressoRepositorio {

	void salvar(TipoIngresso tipoIngresso);

	TipoIngresso obter(TipoIngressoId id);

	void remover(TipoIngressoId id);

	List<TipoIngresso> pesquisarPorEvento(EventoId eventoId);
}
