package cesar.rv.ingressify.dominio.marketplace.denuncia;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;

public class Denuncia {

	private DenunciaId id;
	private final AnuncioRevendaId anuncioId;
	private final UsuarioId denuncianteId;
	private final MotivoDenuncia motivo;
	private final String descricao;
	private StatusDenuncia status;
	private DecisaoModeracao decisao;
	private final LocalDateTime criadaEm;
	private LocalDateTime decididaEm;

	public Denuncia(AnuncioRevendaId anuncioId, UsuarioId denuncianteId, MotivoDenuncia motivo, String descricao,
			LocalDateTime criadaEm) {
		Validate.notNull(anuncioId, "anuncioId");
		Validate.notNull(denuncianteId, "denuncianteId");
		Validate.notNull(motivo, "motivo");
		Validate.notNull(criadaEm, "criadaEm");
		this.anuncioId = anuncioId;
		this.denuncianteId = denuncianteId;
		this.motivo = motivo;
		this.descricao = descricao;
		this.status = StatusDenuncia.PENDENTE;
		this.criadaEm = criadaEm;
	}

	public Denuncia(DenunciaId id, AnuncioRevendaId anuncioId, UsuarioId denuncianteId, MotivoDenuncia motivo,
			String descricao, StatusDenuncia status, DecisaoModeracao decisao, LocalDateTime criadaEm,
			LocalDateTime decididaEm) {
		Validate.notNull(id, "id");
		Validate.notNull(anuncioId, "anuncioId");
		Validate.notNull(denuncianteId, "denuncianteId");
		Validate.notNull(motivo, "motivo");
		Validate.notNull(status, "status");
		Validate.notNull(criadaEm, "criadaEm");
		this.id = id;
		this.anuncioId = anuncioId;
		this.denuncianteId = denuncianteId;
		this.motivo = motivo;
		this.descricao = descricao;
		this.status = status;
		this.decisao = decisao;
		this.criadaEm = criadaEm;
		this.decididaEm = decididaEm;
	}

	public void atribuirId(DenunciaId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void decidir(DecisaoModeracao decisao, LocalDateTime agora) {
		Validate.notNull(decisao, "decisao");
		if (status != StatusDenuncia.PENDENTE) {
			throw new IllegalStateException("denúncia já decidida");
		}
		this.decisao = decisao;
		this.status = StatusDenuncia.RESOLVIDA;
		this.decididaEm = agora;
	}

	public DenunciaId getId() {
		return id;
	}

	public AnuncioRevendaId getAnuncioId() {
		return anuncioId;
	}

	public UsuarioId getDenuncianteId() {
		return denuncianteId;
	}

	public MotivoDenuncia getMotivo() {
		return motivo;
	}

	public String getDescricao() {
		return descricao;
	}

	public StatusDenuncia getStatus() {
		return status;
	}

	public DecisaoModeracao getDecisao() {
		return decisao;
	}

	public LocalDateTime getCriadaEm() {
		return criadaEm;
	}

	public LocalDateTime getDecididaEm() {
		return decididaEm;
	}
}
