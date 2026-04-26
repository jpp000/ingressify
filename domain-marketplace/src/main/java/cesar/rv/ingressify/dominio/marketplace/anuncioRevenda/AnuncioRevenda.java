package cesar.rv.ingressify.dominio.marketplace.anuncioRevenda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

public class AnuncioRevenda {

	private AnuncioRevendaId id;
	private final List<IngressoId> ingressoIds;
	private final int quantidade;
	private UsuarioId vendedor;
	private Dinheiro preco;
	private UsuarioId compradorReservado;
	private StatusAnuncio status;
	private UUID correlacaoPagamento;

	public AnuncioRevenda(List<IngressoId> ingressoIds, UsuarioId vendedor, Dinheiro preco) {
		Validate.notEmpty(ingressoIds, "ingressoIds");
		Validate.isTrue(unicos(ingressoIds) == ingressoIds.size(), "ingressos duplicados no anúncio");
		this.ingressoIds = new ArrayList<>(ingressoIds);
		this.quantidade = this.ingressoIds.size();
		Validate.notNull(vendedor, "vendedor");
		Validate.notNull(preco, "preco");
		this.vendedor = vendedor;
		this.preco = preco;
		this.status = StatusAnuncio.DISPONIVEL;
	}

	private static int unicos(List<IngressoId> ids) {
		return (int) ids.stream().distinct().count();
	}

	public AnuncioRevenda(AnuncioRevendaId id, List<IngressoId> ingressoIds, UsuarioId vendedor, Dinheiro preco,
			UsuarioId compradorReservado, StatusAnuncio status, UUID correlacaoPagamento) {
		Validate.notNull(id, "id");
		Validate.notEmpty(ingressoIds, "ingressoIds");
		Validate.isTrue(unicos(ingressoIds) == ingressoIds.size(), "ingressos duplicados no anúncio");
		Validate.notNull(vendedor, "vendedor");
		Validate.notNull(preco, "preco");
		Validate.notNull(status, "status");

		this.id = id;
		this.ingressoIds = new ArrayList<>(ingressoIds);
		this.quantidade = this.ingressoIds.size();
		this.vendedor = vendedor;
		this.preco = preco;
		this.compradorReservado = compradorReservado;
		this.status = status;
		this.correlacaoPagamento = correlacaoPagamento;
	}

	public void atribuirId(AnuncioRevendaId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void reservar(UsuarioId comprador, UUID correlacao) {
		Validate.notNull(comprador, "comprador");
		if (status != StatusAnuncio.DISPONIVEL) {
			throw new IllegalStateException("anúncio não está disponível");
		}
		this.compradorReservado = comprador;
		this.status = StatusAnuncio.RESERVADO;
		this.correlacaoPagamento = correlacao;
	}

	public void cancelar() {
		if (status == StatusAnuncio.VENDIDO) {
			throw new IllegalStateException("anúncio já vendido");
		}
		this.status = StatusAnuncio.CANCELADO;
	}

	public void cancelarReserva() {
		if (status != StatusAnuncio.RESERVADO) {
			throw new IllegalStateException("anúncio não está reservado");
		}
		this.compradorReservado = null;
		this.correlacaoPagamento = null;
		this.status = StatusAnuncio.DISPONIVEL;
	}

	public void concluir() {
		if (status != StatusAnuncio.RESERVADO) {
			throw new IllegalStateException("anúncio deve estar reservado para concluir");
		}
		this.status = StatusAnuncio.VENDIDO;
	}

	public void alterarPreco(Dinheiro novoPreco) {
		Validate.notNull(novoPreco, "novoPreco");
		if (status != StatusAnuncio.DISPONIVEL) {
			throw new IllegalStateException("só é possível alterar preço em anúncio DISPONIVEL");
		}
		this.preco = novoPreco;
	}

	public AnuncioRevendaId getId() {
		return id;
	}

	public int getQuantidade() {
		return quantidade;
	}

	public List<IngressoId> getIngressoIds() {
		return Collections.unmodifiableList(ingressoIds);
	}

	public UsuarioId getVendedor() {
		return vendedor;
	}

	public Dinheiro getPreco() {
		return preco;
	}

	public UsuarioId getCompradorReservado() {
		return compradorReservado;
	}

	public StatusAnuncio getStatus() {
		return status;
	}

	public UUID getCorrelacaoPagamento() {
		return correlacaoPagamento;
	}
}
