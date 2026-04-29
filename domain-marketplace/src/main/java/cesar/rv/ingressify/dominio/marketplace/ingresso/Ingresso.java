package cesar.rv.ingressify.dominio.marketplace.ingresso;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

public class Ingresso {

	private IngressoId id;
	private TipoIngressoId tipoIngressoId;
	private EventoId eventoId;
	private UsuarioId proprietario;
	private StatusIngresso status;
	private boolean bloqueadoPorReembolso;

	public Ingresso(TipoIngressoId tipoIngressoId, EventoId eventoId, UsuarioId proprietario) {
		Validate.notNull(tipoIngressoId, "tipoIngressoId");
		Validate.notNull(eventoId, "eventoId");
		Validate.notNull(proprietario, "proprietario");
		this.tipoIngressoId = tipoIngressoId;
		this.eventoId = eventoId;
		this.proprietario = proprietario;
		this.status = StatusIngresso.ATIVO;
	}

	public Ingresso(IngressoId id, TipoIngressoId tipoIngressoId, EventoId eventoId, UsuarioId proprietario,
			StatusIngresso status, boolean bloqueadoPorReembolso) {
		Validate.notNull(id, "id");
		Validate.notNull(tipoIngressoId, "tipoIngressoId");
		Validate.notNull(eventoId, "eventoId");
		Validate.notNull(proprietario, "proprietario");
		Validate.notNull(status, "status");
		this.id = id;
		this.tipoIngressoId = tipoIngressoId;
		this.eventoId = eventoId;
		this.proprietario = proprietario;
		this.status = status;
		this.bloqueadoPorReembolso = bloqueadoPorReembolso;
	}

	public void atribuirId(IngressoId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void transferir(UsuarioId novoDono) {
		Validate.notNull(novoDono, "novoDono");
		if (bloqueadoPorReembolso) {
			throw new IllegalStateException("ingresso bloqueado para reembolso");
		}
		if (status != StatusIngresso.ATIVO) {
			throw new IllegalStateException("somente ingresso ATIVO pode ser transferido");
		}
		this.proprietario = novoDono;
	}

	public void marcarEmRevenda() {
		if (bloqueadoPorReembolso) {
			throw new IllegalStateException("ingresso bloqueado para reembolso");
		}
		if (status != StatusIngresso.ATIVO) {
			throw new IllegalStateException("somente ingresso ATIVO pode ir para revenda");
		}
		this.status = StatusIngresso.EM_REVENDA;
	}

	public void concluirRevenda(UsuarioId comprador) {
		Validate.notNull(comprador, "comprador");
		if (status != StatusIngresso.EM_REVENDA) {
			throw new IllegalStateException("ingresso deve estar EM_REVENDA");
		}
		this.proprietario = comprador;
		this.status = StatusIngresso.REVENDIDO;
	}

	public void cancelar() {
		this.status = StatusIngresso.CANCELADO;
	}

	public void desmarcarRevenda() {
		if (status != StatusIngresso.EM_REVENDA) {
			throw new IllegalStateException("esperado EM_REVENDA");
		}
		this.status = StatusIngresso.ATIVO;
	}

	public void bloquearParaReembolso() {
		this.bloqueadoPorReembolso = true;
	}

	public void desbloquearReembolso() {
		this.bloqueadoPorReembolso = false;
	}

	public void marcarUtilizado() {
		if (status != StatusIngresso.ATIVO) {
			throw new IllegalStateException("somente ingresso ATIVO pode ser utilizado");
		}
		this.status = StatusIngresso.UTILIZADO;
	}

	public void marcarReembolsado() {
		if (status == StatusIngresso.UTILIZADO) {
			throw new IllegalStateException("ingresso já utilizado");
		}
		if (status == StatusIngresso.REEMBOLSADO) {
			throw new IllegalStateException("ingresso já reembolsado");
		}
		this.status = StatusIngresso.REEMBOLSADO;
	}

	public IngressoId getId() {
		return id;
	}

	public TipoIngressoId getTipoIngressoId() {
		return tipoIngressoId;
	}

	public EventoId getEventoId() {
		return eventoId;
	}

	public UsuarioId getProprietario() {
		return proprietario;
	}

	public StatusIngresso getStatus() {
		return status;
	}

	public boolean isBloqueadoPorReembolso() {
		return bloqueadoPorReembolso;
	}
}
