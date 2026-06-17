package cesar.rv.ingressify.dominio.marketplace.grupoCompra;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class ParticipanteGrupo {

	private ParticipanteGrupoId id;
	private final GrupoCompraId grupoCompraId;
	private final UsuarioId usuarioId;
	private final int quantidade;
	private final boolean meiaEntrada;
	private final String documento;
	private final Dinheiro valor;
	private StatusParticipanteGrupo status;
	private LocalDateTime pagoEm;

	public ParticipanteGrupo(GrupoCompraId grupoCompraId, UsuarioId usuarioId, int quantidade,
			boolean meiaEntrada, String documento, Dinheiro valor) {
		Validate.notNull(grupoCompraId, "grupoCompraId");
		Validate.notNull(usuarioId, "usuarioId");
		Validate.isTrue(quantidade > 0, "quantidade deve ser > 0");
		if (meiaEntrada) {
			Validate.notBlank(documento, "documento");
		}
		Validate.notNull(valor, "valor");
		this.grupoCompraId = grupoCompraId;
		this.usuarioId = usuarioId;
		this.quantidade = quantidade;
		this.meiaEntrada = meiaEntrada;
		this.documento = documento;
		this.valor = valor;
		this.status = StatusParticipanteGrupo.PENDENTE;
	}

	public ParticipanteGrupo(ParticipanteGrupoId id, GrupoCompraId grupoCompraId, UsuarioId usuarioId, int quantidade,
			boolean meiaEntrada, String documento, Dinheiro valor, StatusParticipanteGrupo status,
			LocalDateTime pagoEm) {
		Validate.notNull(id, "id");
		Validate.notNull(grupoCompraId, "grupoCompraId");
		Validate.notNull(usuarioId, "usuarioId");
		Validate.isTrue(quantidade > 0, "quantidade deve ser > 0");
		Validate.notNull(valor, "valor");
		Validate.notNull(status, "status");
		this.id = id;
		this.grupoCompraId = grupoCompraId;
		this.usuarioId = usuarioId;
		this.quantidade = quantidade;
		this.meiaEntrada = meiaEntrada;
		this.documento = documento;
		this.valor = valor;
		this.status = status;
		this.pagoEm = pagoEm;
	}

	public void atribuirId(ParticipanteGrupoId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void marcarPago() {
		if (status != StatusParticipanteGrupo.PENDENTE) {
			throw new IllegalStateException("participante não está PENDENTE");
		}
		this.status = StatusParticipanteGrupo.PAGO;
		this.pagoEm = LocalDateTime.now();
	}

	public void marcarReembolsado() {
		if (status != StatusParticipanteGrupo.PAGO) {
			throw new IllegalStateException("participante não está PAGO");
		}
		this.status = StatusParticipanteGrupo.REEMBOLSADO;
	}

	public ParticipanteGrupoId getId() {
		return id;
	}

	public GrupoCompraId getGrupoCompraId() {
		return grupoCompraId;
	}

	public UsuarioId getUsuarioId() {
		return usuarioId;
	}

	public int getQuantidade() {
		return quantidade;
	}

	public boolean isMeiaEntrada() {
		return meiaEntrada;
	}

	public String getDocumento() {
		return documento;
	}

	public Dinheiro getValor() {
		return valor;
	}

	public StatusParticipanteGrupo getStatus() {
		return status;
	}

	public LocalDateTime getPagoEm() {
		return pagoEm;
	}
}
