package cesar.rv.ingressify.dominio.marketplace.anuncioRevenda;

import java.util.Optional;
import java.util.UUID;

import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

public interface AnuncioRevendaRepositorio {

	void salvar(AnuncioRevenda anuncio);

	AnuncioRevenda obter(AnuncioRevendaId id);

	void remover(AnuncioRevendaId id);

	Optional<AnuncioRevenda> obterPorCorrelacaoPagamento(UUID correlacao);

	boolean existeDisponivelOuReservadoParaIngresso(IngressoId ingressoId);
}
