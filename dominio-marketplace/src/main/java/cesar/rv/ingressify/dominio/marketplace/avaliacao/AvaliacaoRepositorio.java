package cesar.rv.ingressify.dominio.marketplace.avaliacao;

import java.util.List;

import java.util.Optional;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface AvaliacaoRepositorio {

	Avaliacao obter(AvaliacaoId id);

	void salvar(Avaliacao avaliacao);

	Optional<Avaliacao> buscarPorEventoEUsuario(EventoId eventoId, UsuarioId usuarioId);

	List<Avaliacao> pesquisarPorEvento(EventoId eventoId);
}
