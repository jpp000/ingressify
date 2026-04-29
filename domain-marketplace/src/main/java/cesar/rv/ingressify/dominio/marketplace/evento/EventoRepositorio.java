package cesar.rv.ingressify.dominio.marketplace.evento;

import java.util.Collection;
import java.util.List;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface EventoRepositorio {

	void salvar(Evento evento);

	Evento obter(EventoId id);

	void remover(EventoId id);

	List<Evento> listarAtivos();

	List<Evento> listarPorOrganizador(UsuarioId organizadorId);

	List<Evento> pesquisarPorIds(Collection<EventoId> ids);
}
