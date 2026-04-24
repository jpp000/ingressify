package cesar.rv.ingressify.aplicacao.marketplace.evento;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

@Service
public class EventoServicoAplicacao {

	private final EventoRepositorioAplicacao repositorio;

	public EventoServicoAplicacao(EventoRepositorioAplicacao repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public List<EventoResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}

	public List<EventoResumo> pesquisarAtivosFuturos() {
		return repositorio.pesquisarAtivosFuturos();
	}

	public Optional<EventoResumoExpandido> pesquisarResumoExpandido(EventoId id) {
		return repositorio.pesquisarResumoExpandido(id);
	}
}
