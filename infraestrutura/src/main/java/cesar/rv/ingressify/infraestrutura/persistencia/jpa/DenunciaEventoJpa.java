package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEvento;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEventoId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.MotivoDenunciaEvento;
import cesar.rv.ingressify.dominio.marketplace.denuncia.StatusDenunciaEvento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

@Entity
@Table(name = "denuncias_evento")
public class DenunciaEventoJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "evento_id", nullable = false)
	private Integer eventoId;

	@Column(name = "denunciante_id", nullable = false)
	private Integer denuncianteId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MotivoDenunciaEvento motivo;

	@Column(columnDefinition = "TEXT")
	private String descricao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusDenunciaEvento status;

	@Column(name = "criada_em", nullable = false)
	private LocalDateTime criadaEm;

	@Column(name = "decidida_em")
	private LocalDateTime decididaEm;

	protected DenunciaEventoJpa() {}

	public static DenunciaEventoJpa fromDomain(DenunciaEvento d) {
		var jpa = new DenunciaEventoJpa();
		if (d.getId() != null) jpa.id = d.getId().getId();
		jpa.eventoId = d.getEventoId().getId();
		jpa.denuncianteId = d.getDenuncianteId().getId();
		jpa.motivo = d.getMotivo();
		jpa.descricao = d.getDescricao();
		jpa.status = d.getStatus();
		jpa.criadaEm = d.getCriadaEm();
		jpa.decididaEm = d.getDecididaEm();
		return jpa;
	}

	public DenunciaEvento toDomain() {
		return new DenunciaEvento(new DenunciaEventoId(id), new EventoId(eventoId),
				new UsuarioId(denuncianteId), motivo, descricao, status, criadaEm, decididaEm);
	}

	public Integer getId() { return id; }
	public Integer getEventoId() { return eventoId; }
	public Integer getDenuncianteId() { return denuncianteId; }
	public StatusDenunciaEvento getStatus() { return status; }
}
