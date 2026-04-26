package cesar.rv.ingressify.dominio.marketplace.anuncioRevenda;

import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

public interface AnuncioRevendaRepositorio {

	void salvar(AnuncioRevenda anuncio);

	AnuncioRevenda obter(AnuncioRevendaId id);

	void remover(AnuncioRevendaId id);

	boolean existeDisponivelOuReservadoParaIngresso(IngressoId ingressoId);
}
