package cesar.rv.ingressify.dominio.marketplace.anuncioRevenda;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;

public class AnuncioRevendaServico {

	private final AnuncioRevendaRepositorio anuncioRepositorio;
	private final IngressoRepositorio ingressoRepositorio;
	private final TipoIngressoRepositorio tipoIngressoRepositorio;

	public AnuncioRevendaServico(AnuncioRevendaRepositorio anuncioRepositorio, IngressoRepositorio ingressoRepositorio,
			TipoIngressoRepositorio tipoIngressoRepositorio) {
		Validate.notNull(anuncioRepositorio, "anuncioRepositorio");
		Validate.notNull(ingressoRepositorio, "ingressoRepositorio");
		Validate.notNull(tipoIngressoRepositorio, "tipoIngressoRepositorio");
		this.anuncioRepositorio = anuncioRepositorio;
		this.ingressoRepositorio = ingressoRepositorio;
		this.tipoIngressoRepositorio = tipoIngressoRepositorio;
	}

	public AnuncioRevenda criar(List<IngressoId> ingressoIds, Dinheiro preco, UsuarioId vendedorId) {
		Validate.notEmpty(ingressoIds, "ingressoIds");
		List<Ingresso> ingressos = new ArrayList<>();
		for (IngressoId iid : ingressoIds) {
			ingressos.add(ingressoRepositorio.obter(iid));
		}
		Ingresso referencia = ingressos.get(0);
		TipoIngressoId tipoRef = referencia.getTipoIngressoId();
		EventoId eventoRef = referencia.getEventoId();
		for (Ingresso i : ingressos) {
			if (!i.getProprietario().equals(vendedorId)) {
				throw new IllegalStateException("vendedor não é proprietário do ingresso");
			}
			if (i.getStatus() != StatusIngresso.ATIVO) {
				throw new IllegalStateException("ingresso deve estar ATIVO");
			}
			if (!i.getTipoIngressoId().equals(tipoRef) || !i.getEventoId().equals(eventoRef)) {
				throw new IllegalStateException("todos os ingressos do anúncio devem ser do mesmo tipo e evento");
			}
		}
		for (IngressoId iid : ingressoIds) {
			if (anuncioRepositorio.existeDisponivelOuReservadoParaIngresso(iid)) {
				throw new IllegalStateException("ingresso já possui anúncio ativo");
			}
		}
		validarEsgotamentoVendaOficial(eventoRef);
		for (Ingresso i : ingressos) {
			i.marcarEmRevenda();
			ingressoRepositorio.salvar(i);
		}
		AnuncioRevenda anuncio = new AnuncioRevenda(ingressoIds, vendedorId, preco);
		anuncioRepositorio.salvar(anuncio);
		return anuncio;
	}

	private void validarEsgotamentoVendaOficial(EventoId eventoId) {
		List<TipoIngresso> tipos = tipoIngressoRepositorio.pesquisarPorEvento(eventoId);
		if (tipos.isEmpty()) {
			throw new IllegalStateException("evento sem tipos de ingresso oficiais");
		}
		for (TipoIngresso t : tipos) {
			if (t.getQuantidadeDisponivel() > 0) {
				throw new IllegalStateException("venda oficial do evento ainda não esgotou");
			}
		}
	}

	public AnuncioRevenda reservar(AnuncioRevendaId anuncioId, UsuarioId comprador, UUID correlacaoPagamento) {
		AnuncioRevenda a = anuncioRepositorio.obter(anuncioId);
		a.reservar(comprador, correlacaoPagamento);
		anuncioRepositorio.salvar(a);
		return a;
	}

	public void cancelar(AnuncioRevendaId id) {
		AnuncioRevenda a = anuncioRepositorio.obter(id);
		a.cancelar();
		for (IngressoId iid : a.getIngressoIds()) {
			Ingresso ingresso = ingressoRepositorio.obter(iid);
			if (ingresso.getStatus() == StatusIngresso.EM_REVENDA) {
				ingresso.desmarcarRevenda();
				ingressoRepositorio.salvar(ingresso);
			}
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
}
