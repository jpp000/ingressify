package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

@Entity
@Table(name = "ingressos")
public class IngressoJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "tipo_ingresso_id", nullable = false)
	private Integer tipoIngressoId;

	@Column(name = "evento_id", nullable = false)
	private Integer eventoId;

	@Column(name = "proprietario_id", nullable = false)
	private Integer proprietarioId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusIngresso status;

	@Column(name = "bloqueado_por_reembolso", nullable = false)
	private boolean bloqueadoPorReembolso;

	@Column(name = "meia_entrada", nullable = false)
	private boolean meiaEntrada;

	@Column(name = "documento")
	private String documento;

	protected IngressoJpa() {}

	public static IngressoJpa fromDomain(Ingresso i) {
		var jpa = new IngressoJpa();
		if (i.getId() != null) jpa.id = i.getId().getId();
		jpa.tipoIngressoId = i.getTipoIngressoId().getId();
		jpa.eventoId = i.getEventoId().getId();
		jpa.proprietarioId = i.getProprietario().getId();
		jpa.status = i.getStatus();
		jpa.bloqueadoPorReembolso = i.isBloqueadoPorReembolso();
		jpa.meiaEntrada = i.isMeiaEntrada();
		jpa.documento = i.getDocumento();
		return jpa;
	}

	public Ingresso toDomain() {
		Ingresso ingresso = new Ingresso(
				new IngressoId(id), new TipoIngressoId(tipoIngressoId),
				new EventoId(eventoId), new UsuarioId(proprietarioId),
				status, bloqueadoPorReembolso);
		ingresso.reconstituirMeiaEntrada(meiaEntrada, documento);
		return ingresso;
	}

	public UUID getId() { return id; }
	public Integer getTipoIngressoId() { return tipoIngressoId; }
	public Integer getEventoId() { return eventoId; }
	public Integer getProprietarioId() { return proprietarioId; }
	public StatusIngresso getStatus() { return status; }
}
