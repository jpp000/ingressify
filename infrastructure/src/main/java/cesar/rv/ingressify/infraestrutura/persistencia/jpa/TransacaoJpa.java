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
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoId;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

@Entity
@Table(name = "transacoes")
public class TransacaoJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "usuario_id", nullable = false)
	private Integer usuarioId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoTransacao tipo;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal valor;

	@Column(nullable = false)
	private LocalDateTime data;

	@Column(name = "referencia_externa_id", nullable = false)
	private UUID referenciaExternaId;

	protected TransacaoJpa() {}

	public static TransacaoJpa fromDomain(Transacao t) {
		var jpa = new TransacaoJpa();
		if (t.getId() != null) jpa.id = t.getId().getId();
		jpa.usuarioId = t.getUsuario().getId();
		jpa.tipo = t.getTipo();
		jpa.valor = t.getValor().getValor();
		jpa.data = t.getData();
		jpa.referenciaExternaId = t.getReferenciaExternaId();
		return jpa;
	}

	public Transacao toDomain() {
		return new Transacao(
				new TransacaoId(id), new UsuarioId(usuarioId), tipo,
				new Dinheiro(valor), data, referenciaExternaId);
	}

	public Integer getId() { return id; }
	public Integer getUsuarioId() { return usuarioId; }
	public LocalDateTime getData() { return data; }
}
