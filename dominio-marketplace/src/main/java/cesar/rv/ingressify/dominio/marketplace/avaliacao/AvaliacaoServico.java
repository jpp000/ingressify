package cesar.rv.ingressify.dominio.marketplace.avaliacao;

import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public class AvaliacaoServico {

	private final AvaliacaoRepositorio repositorio;

	public AvaliacaoServico(AvaliacaoRepositorio repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public Avaliacao obter(AvaliacaoId id) {
		return repositorio.obter(id);
	}

	public void salvar(Avaliacao avaliacao) {
		repositorio.salvar(avaliacao);
	}

	public void responder(AvaliacaoId id, String texto, java.time.LocalDateTime agora) {
		Avaliacao a = repositorio.obter(id);
		a.responder(texto, agora);
		repositorio.salvar(a);
	}

	public double mediaPorEvento(EventoId eventoId) {
		List<Avaliacao> lista = repositorio.pesquisarPorEvento(eventoId);
		if (lista.isEmpty()) {
			return 0.0;
		}
		return lista.stream().mapToInt(Avaliacao::getNota).average().orElse(0.0);
	}
}
