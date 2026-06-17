package cesar.rv.ingressify.aplicacao.marketplace.anuncioRevenda;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface AnuncioRevendaResumo {

	AnuncioRevendaId getId();

	EventoId getEventoId();

	String getTipoNome();

	int getQuantidade();

	Dinheiro getPreco();

	StatusAnuncio getStatus();

	UsuarioId getVendedorId();
}
