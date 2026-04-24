package cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

@Service
public class TipoIngressoServicoAplicacao {

	private final TipoIngressoRepositorioAplicacao repositorio;

	public TipoIngressoServicoAplicacao(TipoIngressoRepositorioAplicacao repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public List<TipoIngressoResumo> pesquisarResumosPorEvento(EventoId eventoId) {
		return repositorio.pesquisarResumosPorEvento(eventoId);
	}

	public List<TipoIngressoResumo> pesquisarResumos() {
		return repositorio.pesquisarResumos();
	}

	public TipoIngressoResumo obterResumo(TipoIngressoId id) {
		return repositorio.obterResumo(id);
	}
}
