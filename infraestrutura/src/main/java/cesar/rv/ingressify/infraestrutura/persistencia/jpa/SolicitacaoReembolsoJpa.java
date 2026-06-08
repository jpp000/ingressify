package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.reembolso.MotivoReembolso;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolso;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoId;
import cesar.rv.ingressify.dominio.marketplace.reembolso.StatusSolicitacaoReembolso;

@Entity
@Table(name = "solicitacoes_reembolso")
public class SolicitacaoReembolsoJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "ingresso_id", nullable = false)
	private UUID ingressoId;

	@Column(name = "solicitante_id", nullable = false)
	private Integer solicitanteId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MotivoReembolso motivo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusSolicitacaoReembolso status;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal valor;

	@Column(name = "criada_em", nullable = false)
	private LocalDateTime criadaEm;

	@Column(name = "decidida_em")
	private LocalDateTime decididaEm;

	protected SolicitacaoReembolsoJpa() {}

	public static SolicitacaoReembolsoJpa fromDomain(SolicitacaoReembolso s) {
		var jpa = new SolicitacaoReembolsoJpa();
		if (s.getId() != null) jpa.id = s.getId().getId();
		jpa.ingressoId = s.getIngressoId().getId();
		jpa.solicitanteId = s.getSolicitanteId().getId();
		jpa.motivo = s.getMotivo();
		jpa.status = s.getStatus();
		jpa.valor = s.getValor().getValor();
		jpa.criadaEm = s.getCriadaEm();
		jpa.decididaEm = s.getDecididaEm();
		return jpa;
	}

	public SolicitacaoReembolso toDomain() {
		return new SolicitacaoReembolso(
				new SolicitacaoReembolsoId(id), new IngressoId(ingressoId),
				new UsuarioId(solicitanteId), motivo, status,
				new Dinheiro(valor), criadaEm, decididaEm);
	}

	public Integer getId() { return id; }
	public UUID getIngressoId() { return ingressoId; }
	public Integer getSolicitanteId() { return solicitanteId; }
	public StatusSolicitacaoReembolso getStatus() { return status; }
}
