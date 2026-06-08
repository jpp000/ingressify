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
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DecisaoModeracao;
import cesar.rv.ingressify.dominio.marketplace.denuncia.Denuncia;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.MotivoDenuncia;
import cesar.rv.ingressify.dominio.marketplace.denuncia.StatusDenuncia;

@Entity
@Table(name = "denuncias")
public class DenunciaJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "anuncio_id", nullable = false)
	private Integer anuncioId;

	@Column(name = "denunciante_id", nullable = false)
	private Integer denuncianteId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MotivoDenuncia motivo;

	@Column(columnDefinition = "TEXT")
	private String descricao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusDenuncia status;

	@Enumerated(EnumType.STRING)
	@Column
	private DecisaoModeracao decisao;

	@Column(name = "criada_em", nullable = false)
	private LocalDateTime criadaEm;

	@Column(name = "decidida_em")
	private LocalDateTime decididaEm;

	protected DenunciaJpa() {}

	public static DenunciaJpa fromDomain(Denuncia d) {
		var jpa = new DenunciaJpa();
		if (d.getId() != null) jpa.id = d.getId().getId();
		jpa.anuncioId = d.getAnuncioId().getId();
		jpa.denuncianteId = d.getDenuncianteId().getId();
		jpa.motivo = d.getMotivo();
		jpa.descricao = d.getDescricao();
		jpa.status = d.getStatus();
		jpa.decisao = d.getDecisao();
		jpa.criadaEm = d.getCriadaEm();
		jpa.decididaEm = d.getDecididaEm();
		return jpa;
	}

	public Denuncia toDomain() {
		return new Denuncia(new DenunciaId(id), new AnuncioRevendaId(anuncioId),
				new UsuarioId(denuncianteId), motivo, descricao, status, decisao, criadaEm, decididaEm);
	}

	public Integer getId() { return id; }
	public Integer getAnuncioId() { return anuncioId; }
	public Integer getDenuncianteId() { return denuncianteId; }
	public StatusDenuncia getStatus() { return status; }
}
