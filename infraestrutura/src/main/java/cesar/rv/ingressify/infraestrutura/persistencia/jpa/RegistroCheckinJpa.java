package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckin;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

@Entity
@Table(name = "registros_checkin")
public class RegistroCheckinJpa {

	@Id
	@Column(nullable = false)
	private UUID id;

	@Column(name = "ingresso_id", nullable = false)
	private UUID ingressoId;

	@Column(name = "evento_id", nullable = false)
	private Integer eventoId;

	@Column(name = "operador_id", nullable = false)
	private Integer operadorId;

	@Column(name = "data_hora", nullable = false)
	private LocalDateTime dataHora;

	protected RegistroCheckinJpa() {}

	public static RegistroCheckinJpa fromDomain(RegistroCheckin r) {
		var jpa = new RegistroCheckinJpa();
		jpa.id = r.getId();
		jpa.ingressoId = r.getIngressoId().getId();
		jpa.eventoId = r.getEventoId().getId();
		jpa.operadorId = r.getOperadorId().getId();
		jpa.dataHora = r.getDataHora();
		return jpa;
	}

	public RegistroCheckin toDomain() {
		return new RegistroCheckin(id, new IngressoId(ingressoId),
				new EventoId(eventoId), new UsuarioId(operadorId), dataHora);
	}

	public UUID getId() { return id; }
	public Integer getEventoId() { return eventoId; }
}
