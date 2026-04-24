package cesar.rv.ingressify.aplicacao.marketplace.evento;

import java.util.List;
import java.util.Optional;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface EventoRepositorioAplicacao {

	List<EventoResumo> pesquisarResumos();

	List<EventoResumo> pesquisarAtivosFuturos();

	Optional<EventoResumoExpandido> pesquisarResumoExpandido(EventoId id);
}
