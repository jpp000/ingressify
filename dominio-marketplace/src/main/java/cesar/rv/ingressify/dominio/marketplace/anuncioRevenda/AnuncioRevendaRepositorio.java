package cesar.rv.ingressify.dominio.marketplace.anuncioRevenda;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

public interface AnuncioRevendaRepositorio {

	void salvar(AnuncioRevenda anuncio);

	AnuncioRevenda obter(AnuncioRevendaId id);

	void remover(AnuncioRevendaId id);

	boolean existeDisponivelOuReservadoParaIngresso(IngressoId ingressoId);

	boolean existeDisponivelOuReservadoParaEvento(EventoId eventoId);

	List<AnuncioRevenda> pesquisarPorEvento(EventoId eventoId);
}
