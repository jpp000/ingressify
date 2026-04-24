package cesar.rv.ingressify.aplicacao.marketplace.anuncio;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;

public interface AnuncioRepositorioAplicacao {

	List<AnuncioResumo> pesquisarResumos();

	AnuncioResumoExpandido pesquisarResumoExpandido(AnuncioRevendaId id);
}
