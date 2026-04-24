package cesar.rv.ingressify.dominio.marketplace.anuncioRevenda;

import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;

public class AnuncioRevendaServico {

	private final AnuncioRevendaRepositorio anuncioRepositorio;
	private final IngressoRepositorio ingressoRepositorio;

	public AnuncioRevendaServico(AnuncioRevendaRepositorio anuncioRepositorio, IngressoRepositorio ingressoRepositorio) {
		Validate.notNull(anuncioRepositorio, "anuncioRepositorio");
		Validate.notNull(ingressoRepositorio, "ingressoRepositorio");
		this.anuncioRepositorio = anuncioRepositorio;
		this.ingressoRepositorio = ingressoRepositorio;
	}

	public AnuncioRevenda criar(IngressoId ingressoId, Dinheiro preco, UsuarioId vendedorId) {
		Ingresso ingresso = ingressoRepositorio.obter(ingressoId);
		if (!ingresso.getProprietario().equals(vendedorId)) {
			throw new IllegalStateException("vendedor não é proprietário do ingresso");
		}
		if (ingresso.getStatus() != StatusIngresso.ATIVO) {
			throw new IllegalStateException("ingresso deve estar ATIVO");
		}
		if (anuncioRepositorio.existeDisponivelOuReservadoParaIngresso(ingressoId)) {
			throw new IllegalStateException("ingresso já possui anúncio ativo");
		}
		ingresso.marcarEmRevenda();
		ingressoRepositorio.salvar(ingresso);
		AnuncioRevenda anuncio = new AnuncioRevenda(ingressoId, vendedorId, preco);
		anuncioRepositorio.salvar(anuncio);
		return anuncio;
	}

	public AnuncioRevenda reservar(AnuncioRevendaId anuncioId, UsuarioId comprador, UUID correlacaoPagamento) {
		AnuncioRevenda a = anuncioRepositorio.obter(anuncioId);
		a.reservar(comprador, correlacaoPagamento);
		anuncioRepositorio.salvar(a);
		return a;
	}

	public void cancelar(AnuncioRevendaId id) {
		AnuncioRevenda a = anuncioRepositorio.obter(id);
		Ingresso ingresso = ingressoRepositorio.obter(a.getIngressoId());
		a.cancelar();
		if (ingresso.getStatus() == StatusIngresso.EM_REVENDA) {
			ingresso.desmarcarRevenda();
			ingressoRepositorio.salvar(ingresso);
		}
		anuncioRepositorio.salvar(a);
	}

	public void cancelarReserva(AnuncioRevendaId id) {
		AnuncioRevenda a = anuncioRepositorio.obter(id);
		a.cancelarReserva();
		anuncioRepositorio.salvar(a);
	}

	public void alterarPreco(AnuncioRevendaId id, Dinheiro novoPreco) {
		AnuncioRevenda a = anuncioRepositorio.obter(id);
		a.alterarPreco(novoPreco);
		anuncioRepositorio.salvar(a);
	}

	public void concluir(AnuncioRevendaId id) {
		AnuncioRevenda a = anuncioRepositorio.obter(id);
		a.concluir();
		anuncioRepositorio.salvar(a);
	}

	public AnuncioRevenda obter(AnuncioRevendaId id) {
		return anuncioRepositorio.obter(id);
	}

	public AnuncioRevenda obterPorCorrelacao(UUID correlacao) {
		return anuncioRepositorio.obterPorCorrelacaoPagamento(correlacao)
				.orElseThrow(() -> new IllegalArgumentException("anúncio não encontrado para correlação"));
	}

	public void cancelarReservaPorCorrelacao(UUID correlacao) {
		anuncioRepositorio.obterPorCorrelacaoPagamento(correlacao).ifPresent(a -> cancelarReserva(a.getId()));
	}
}
