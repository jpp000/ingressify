package cesar.rv.ingressify.aplicacao.marketplace.evento;

import java.util.List;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface EventoRepositorioAplicacao {

	List<EventoResumo> pesquisarResumosPorOrganizador(UsuarioId organizadorId);

	EventoResumoExpandido obterExpandido(EventoId eventoId);
}
