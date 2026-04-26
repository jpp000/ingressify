package cesar.rv.ingressify.dominio.marketplace.tipoIngresso;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;

public class TipoIngressoServico {

	private final TipoIngressoRepositorio repositorio;
	private final EventoRepositorio eventoRepositorio;

	public TipoIngressoServico(TipoIngressoRepositorio repositorio, EventoRepositorio eventoRepositorio) {
		Validate.notNull(repositorio, "repositorio");
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		this.repositorio = repositorio;
		this.eventoRepositorio = eventoRepositorio;
	}

	public void salvar(TipoIngresso tipo) {
		Evento evento = eventoRepositorio.obter(tipo.getEventoId());
		int somaOutros = repositorio.pesquisarPorEvento(tipo.getEventoId()).stream()
				.filter(t -> tipo.getId() == null || !t.getId().equals(tipo.getId()))
				.mapToInt(TipoIngresso::getQuantidadeTotal)
				.sum();
		Validate.isTrue(somaOutros + tipo.getQuantidadeTotal() <= evento.getCapacidade(),
				"soma das quantidades não pode exceder capacidade do evento");
		repositorio.salvar(tipo);
	}

	public TipoIngresso obter(TipoIngressoId id) {
		return repositorio.obter(id);
	}

	public void devolver(TipoIngressoId id, int qtd) {
		TipoIngresso t = repositorio.obter(id);
		t.devolver(qtd);
		repositorio.salvar(t);
	}
}
