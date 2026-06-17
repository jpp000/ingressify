package cesar.rv.ingressify.dominio.marketplace.denuncia;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public class DenunciaEvento {

	private DenunciaEventoId id;
	private final EventoId eventoId;
	private final UsuarioId denuncianteId;
	private final MotivoDenunciaEvento motivo;
	private final String descricao;
	private StatusDenunciaEvento status;
	private final LocalDateTime criadaEm;
	private LocalDateTime decididaEm;

	public DenunciaEvento(EventoId eventoId, UsuarioId denuncianteId, MotivoDenunciaEvento motivo,
			String descricao, LocalDateTime criadaEm) {
		Validate.notNull(eventoId, "eventoId");
		Validate.notNull(denuncianteId, "denuncianteId");
		Validate.notNull(motivo, "motivo");
		Validate.notNull(criadaEm, "criadaEm");
		this.eventoId = eventoId;
		this.denuncianteId = denuncianteId;
		this.motivo = motivo;
		this.descricao = descricao;
		this.status = StatusDenunciaEvento.PENDENTE;
		this.criadaEm = criadaEm;
	}

	public DenunciaEvento(DenunciaEventoId id, EventoId eventoId, UsuarioId denuncianteId,
			MotivoDenunciaEvento motivo, String descricao, StatusDenunciaEvento status,
			LocalDateTime criadaEm, LocalDateTime decididaEm) {
		Validate.notNull(id, "id");
		Validate.notNull(eventoId, "eventoId");
		Validate.notNull(denuncianteId, "denuncianteId");
		Validate.notNull(motivo, "motivo");
		Validate.notNull(status, "status");
		Validate.notNull(criadaEm, "criadaEm");
		this.id = id;
		this.eventoId = eventoId;
		this.denuncianteId = denuncianteId;
		this.motivo = motivo;
		this.descricao = descricao;
		this.status = status;
		this.criadaEm = criadaEm;
		this.decididaEm = decididaEm;
	}

	public void atribuirId(DenunciaEventoId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void iniciarAnalise(LocalDateTime agora) {
		if (status != StatusDenunciaEvento.PENDENTE) {
			throw new IllegalStateException("denúncia não está pendente");
		}
		this.status = StatusDenunciaEvento.EM_ANALISE;
		this.decididaEm = agora;
	}

	public void aprovar(LocalDateTime agora) {
		if (status != StatusDenunciaEvento.PENDENTE && status != StatusDenunciaEvento.EM_ANALISE) {
			throw new IllegalStateException("denúncia já decidida");
		}
		this.status = StatusDenunciaEvento.APROVADA;
		this.decididaEm = agora;
	}

	public void rejeitar(LocalDateTime agora) {
		if (status != StatusDenunciaEvento.PENDENTE && status != StatusDenunciaEvento.EM_ANALISE) {
			throw new IllegalStateException("denúncia já decidida");
		}
		this.status = StatusDenunciaEvento.REJEITADA;
		this.decididaEm = agora;
	}

	public DenunciaEventoId getId() { return id; }
	public EventoId getEventoId() { return eventoId; }
	public UsuarioId getDenuncianteId() { return denuncianteId; }
	public MotivoDenunciaEvento getMotivo() { return motivo; }
	public String getDescricao() { return descricao; }
	public StatusDenunciaEvento getStatus() { return status; }
	public LocalDateTime getCriadaEm() { return criadaEm; }
	public LocalDateTime getDecididaEm() { return decididaEm; }
}
