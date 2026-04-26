package cesar.rv.ingressify.dominio.marketplace.compra;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

public class Pedido {

	private final UUID id;
	private final TipoIngressoId tipoIngressoId;
	private final EventoId eventoId;
	private final int quantidade;
	private final UsuarioId comprador;
	private final Dinheiro valorTotal;
	private final LocalDateTime criadaEm;

	public Pedido(UUID id, TipoIngressoId tipoIngressoId, EventoId eventoId, int quantidade, UsuarioId comprador,
			Dinheiro valorTotal, LocalDateTime criadaEm) {
		Validate.notNull(id, "id");
		Validate.notNull(tipoIngressoId, "tipoIngressoId");
		Validate.notNull(eventoId, "eventoId");
		Validate.isTrue(quantidade > 0, "quantidade deve ser > 0");
		Validate.notNull(comprador, "comprador");
		Validate.notNull(valorTotal, "valorTotal");
		Validate.notNull(criadaEm, "criadaEm");
		this.id = id;
		this.tipoIngressoId = tipoIngressoId;
		this.eventoId = eventoId;
		this.quantidade = quantidade;
		this.comprador = comprador;
		this.valorTotal = valorTotal;
		this.criadaEm = criadaEm;
	}

	public UUID getId() {
		return id;
	}

	public TipoIngressoId getTipoIngressoId() {
		return tipoIngressoId;
	}

	public EventoId getEventoId() {
		return eventoId;
	}

	public int getQuantidade() {
		return quantidade;
	}

	public UsuarioId getComprador() {
		return comprador;
	}

	public Dinheiro getValorTotal() {
		return valorTotal;
	}

	public LocalDateTime getCriadaEm() {
		return criadaEm;
	}
}
