package cesar.rv.ingressify.dominio.marketplace.denuncia;

import java.util.List;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface DenunciaEventoRepositorio {

	void salvar(DenunciaEvento denuncia);

	DenunciaEvento obter(DenunciaEventoId id);

	List<DenunciaEvento> pesquisarTodas();

	List<DenunciaEvento> pesquisarPorEvento(EventoId eventoId);

	boolean existePorEventoEDenunciante(EventoId eventoId, UsuarioId denuncianteId);
}
