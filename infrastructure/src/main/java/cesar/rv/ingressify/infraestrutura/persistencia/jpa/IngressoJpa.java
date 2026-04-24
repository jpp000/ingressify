package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.UUID;

import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ingresso")
public class IngressoJpa {

	@Id
	@Column(columnDefinition = "uuid", nullable = false)
	private UUID id;

	@Column(name = "tipo_ingresso_id", nullable = false)
	private Integer tipoIngressoId;

	@Column(name = "evento_id", nullable = false)
	private Integer eventoId;

	@Column(name = "proprietario_id", nullable = false)
	private Integer proprietarioId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private StatusIngresso status;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Integer getTipoIngressoId() {
		return tipoIngressoId;
	}

	public void setTipoIngressoId(Integer tipoIngressoId) {
		this.tipoIngressoId = tipoIngressoId;
	}

	public Integer getEventoId() {
		return eventoId;
	}

	public void setEventoId(Integer eventoId) {
		this.eventoId = eventoId;
	}

	public Integer getProprietarioId() {
		return proprietarioId;
	}

	public void setProprietarioId(Integer proprietarioId) {
		this.proprietarioId = proprietarioId;
	}

	public StatusIngresso getStatus() {
		return status;
	}

	public void setStatus(StatusIngresso status) {
		this.status = status;
	}
}
