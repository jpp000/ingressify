package cesar.rv.ingressify.dominio.marketplace.evento;

import java.util.List;

public interface EventoRepositorio {

	void salvar(Evento evento);

	Evento obter(EventoId id);

	void remover(EventoId id);

	List<Evento> listarAtivos();
}
