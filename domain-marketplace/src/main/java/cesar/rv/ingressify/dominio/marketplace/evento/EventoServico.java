package cesar.rv.ingressify.dominio.marketplace.evento;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.Validate;

public class EventoServico {

	private final EventoRepositorio repositorio;

	public EventoServico(EventoRepositorio repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public void salvar(Evento evento) {
		repositorio.salvar(evento);
	}

	public Evento obter(EventoId id) {
		return repositorio.obter(id);
	}

	public void cancelar(EventoId id) {
		Evento e = repositorio.obter(id);
		e.cancelar();
		repositorio.salvar(e);
	}

	public void atualizar(EventoId id, String nome, LocalDateTime dataHora, String local, String descricao, int capacidade) {
		Evento e = repositorio.obter(id);
		e.atualizar(nome, dataHora, local, descricao, capacidade);
		repositorio.salvar(e);
	}

	public void remover(EventoId id) {
		repositorio.remover(id);
	}

	public List<Evento> listarAtivos() {
		return repositorio.listarAtivos().stream()
				.filter(Evento::ativo)
				.toList();
	}
}
