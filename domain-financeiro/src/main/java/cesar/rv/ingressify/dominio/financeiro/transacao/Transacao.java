package cesar.rv.ingressify.dominio.financeiro.transacao;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class Transacao {

	private TransacaoId id;
	private UsuarioId usuario;
	private TipoTransacao tipo;
	private Dinheiro valor;
	private LocalDateTime data;
	private UUID referenciaExternaId;

	public Transacao(UsuarioId usuario, TipoTransacao tipo, Dinheiro valor, LocalDateTime data,
			UUID referenciaExternaId) {
		Validate.notNull(usuario, "usuario");
		Validate.notNull(tipo, "tipo");
		Validate.notNull(valor, "valor");
		Validate.notNull(data, "data");
		Validate.notNull(referenciaExternaId, "referenciaExternaId");
		this.usuario = usuario;
		this.tipo = tipo;
		this.valor = valor;
		this.data = data;
		this.referenciaExternaId = referenciaExternaId;
	}

	public Transacao(TransacaoId id, UsuarioId usuario, TipoTransacao tipo, Dinheiro valor, LocalDateTime data,
			UUID referenciaExternaId) {
		Validate.notNull(id, "id");
		Validate.notNull(usuario, "usuario");
		Validate.notNull(tipo, "tipo");
		Validate.notNull(valor, "valor");
		Validate.notNull(data, "data");
		Validate.notNull(referenciaExternaId, "referenciaExternaId");
		this.id = id;
		this.usuario = usuario;
		this.tipo = tipo;
		this.valor = valor;
		this.data = data;
		this.referenciaExternaId = referenciaExternaId;
	}

	public void atribuirId(TransacaoId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public TransacaoId getId() {
		return id;
	}

	public UsuarioId getUsuario() {
		return usuario;
	}

	public TipoTransacao getTipo() {
		return tipo;
	}

	public Dinheiro getValor() {
		return valor;
	}

	public LocalDateTime getData() {
		return data;
	}

	public UUID getReferenciaExternaId() {
		return referenciaExternaId;
	}
}
