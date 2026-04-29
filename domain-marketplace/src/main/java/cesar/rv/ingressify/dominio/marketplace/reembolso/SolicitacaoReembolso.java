package cesar.rv.ingressify.dominio.marketplace.reembolso;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

public class SolicitacaoReembolso {

	private SolicitacaoReembolsoId id;
	private final IngressoId ingressoId;
	private final UsuarioId solicitanteId;
	private final MotivoReembolso motivo;
	private StatusSolicitacaoReembolso status;
	private final Dinheiro valor;
	private final LocalDateTime criadaEm;
	private LocalDateTime decididaEm;

	public SolicitacaoReembolso(IngressoId ingressoId, UsuarioId solicitanteId, MotivoReembolso motivo,
			Dinheiro valor, LocalDateTime criadaEm) {
		Validate.notNull(ingressoId, "ingressoId");
		Validate.notNull(solicitanteId, "solicitanteId");
		Validate.notNull(motivo, "motivo");
		Validate.notNull(valor, "valor");
		Validate.notNull(criadaEm, "criadaEm");
		this.ingressoId = ingressoId;
		this.solicitanteId = solicitanteId;
		this.motivo = motivo;
		this.valor = valor;
		this.status = StatusSolicitacaoReembolso.PENDENTE;
		this.criadaEm = criadaEm;
	}

	public SolicitacaoReembolso(SolicitacaoReembolsoId id, IngressoId ingressoId, UsuarioId solicitanteId,
			MotivoReembolso motivo, StatusSolicitacaoReembolso status, Dinheiro valor, LocalDateTime criadaEm,
			LocalDateTime decididaEm) {
		Validate.notNull(id, "id");
		Validate.notNull(ingressoId, "ingressoId");
		Validate.notNull(solicitanteId, "solicitanteId");
		Validate.notNull(motivo, "motivo");
		Validate.notNull(status, "status");
		Validate.notNull(valor, "valor");
		Validate.notNull(criadaEm, "criadaEm");
		this.id = id;
		this.ingressoId = ingressoId;
		this.solicitanteId = solicitanteId;
		this.motivo = motivo;
		this.status = status;
		this.valor = valor;
		this.criadaEm = criadaEm;
		this.decididaEm = decididaEm;
	}

	public void atribuirId(SolicitacaoReembolsoId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void aprovar(LocalDateTime agora) {
		if (status != StatusSolicitacaoReembolso.PENDENTE) {
			throw new IllegalStateException("solicitação não está pendente");
		}
		this.status = StatusSolicitacaoReembolso.APROVADA;
		this.decididaEm = agora;
	}

	public void recusar(LocalDateTime agora) {
		if (status != StatusSolicitacaoReembolso.PENDENTE) {
			throw new IllegalStateException("solicitação não está pendente");
		}
		this.status = StatusSolicitacaoReembolso.RECUSADA;
		this.decididaEm = agora;
	}

	public SolicitacaoReembolsoId getId() {
		return id;
	}

	public IngressoId getIngressoId() {
		return ingressoId;
	}

	public UsuarioId getSolicitanteId() {
		return solicitanteId;
	}

	public MotivoReembolso getMotivo() {
		return motivo;
	}

	public StatusSolicitacaoReembolso getStatus() {
		return status;
	}

	public Dinheiro getValor() {
		return valor;
	}

	public LocalDateTime getCriadaEm() {
		return criadaEm;
	}

	public LocalDateTime getDecididaEm() {
		return decididaEm;
	}
}
