package cesar.rv.ingressify.dominio.marketplace.grupoCompra;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

public class GrupoCompra {

	private GrupoCompraId id;
	private final EventoId eventoId;
	private final TipoIngressoId tipoIngressoId;
	private final UsuarioId liderId;
	private final int quantidadeTotal;
	private final LocalDateTime prazoPagamento;
	private StatusGrupoCompra status;
	private final LocalDateTime criadoEm;

	public GrupoCompra(EventoId eventoId, TipoIngressoId tipoIngressoId, UsuarioId liderId,
			int quantidadeTotal, LocalDateTime prazoPagamento) {
		Validate.notNull(eventoId, "eventoId");
		Validate.notNull(tipoIngressoId, "tipoIngressoId");
		Validate.notNull(liderId, "liderId");
		Validate.isTrue(quantidadeTotal > 0, "quantidadeTotal deve ser > 0");
		Validate.notNull(prazoPagamento, "prazoPagamento");
		Validate.isTrue(prazoPagamento.isAfter(LocalDateTime.now()), "prazoPagamento deve ser futuro");
		this.eventoId = eventoId;
		this.tipoIngressoId = tipoIngressoId;
		this.liderId = liderId;
		this.quantidadeTotal = quantidadeTotal;
		this.prazoPagamento = prazoPagamento;
		this.status = StatusGrupoCompra.ABERTO;
		this.criadoEm = LocalDateTime.now();
	}

	public GrupoCompra(GrupoCompraId id, EventoId eventoId, TipoIngressoId tipoIngressoId, UsuarioId liderId,
			int quantidadeTotal, LocalDateTime prazoPagamento, StatusGrupoCompra status, LocalDateTime criadoEm) {
		Validate.notNull(id, "id");
		Validate.notNull(eventoId, "eventoId");
		Validate.notNull(tipoIngressoId, "tipoIngressoId");
		Validate.notNull(liderId, "liderId");
		Validate.isTrue(quantidadeTotal > 0, "quantidadeTotal deve ser > 0");
		Validate.notNull(prazoPagamento, "prazoPagamento");
		Validate.notNull(status, "status");
		Validate.notNull(criadoEm, "criadoEm");
		this.id = id;
		this.eventoId = eventoId;
		this.tipoIngressoId = tipoIngressoId;
		this.liderId = liderId;
		this.quantidadeTotal = quantidadeTotal;
		this.prazoPagamento = prazoPagamento;
		this.status = status;
		this.criadoEm = criadoEm;
	}

	public void atribuirId(GrupoCompraId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void confirmar() {
		if (status != StatusGrupoCompra.ABERTO) {
			throw new IllegalStateException("grupo de compra não está ABERTO");
		}
		this.status = StatusGrupoCompra.CONFIRMADO;
	}

	public void expirar() {
		if (status != StatusGrupoCompra.ABERTO) {
			throw new IllegalStateException("grupo de compra não está ABERTO");
		}
		this.status = StatusGrupoCompra.EXPIRADO;
	}

	public void cancelar() {
		if (status != StatusGrupoCompra.ABERTO) {
			throw new IllegalStateException("grupo de compra não está ABERTO");
		}
		this.status = StatusGrupoCompra.CANCELADO;
	}

	public boolean prazoExpirado(LocalDateTime agora) {
		return status == StatusGrupoCompra.ABERTO && agora.isAfter(prazoPagamento);
	}

	public GrupoCompraId getId() {
		return id;
	}

	public EventoId getEventoId() {
		return eventoId;
	}

	public TipoIngressoId getTipoIngressoId() {
		return tipoIngressoId;
	}

	public UsuarioId getLiderId() {
		return liderId;
	}

	public int getQuantidadeTotal() {
		return quantidadeTotal;
	}

	public LocalDateTime getPrazoPagamento() {
		return prazoPagamento;
	}

	public StatusGrupoCompra getStatus() {
		return status;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}
}
