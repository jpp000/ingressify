package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transacao")
public class TransacaoJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "usuario_id", nullable = false)
	private Integer usuarioId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TipoTransacao tipo;

	@Embedded
	private DinheiroJpa valor = new DinheiroJpa();

	@Column(nullable = false)
	private LocalDateTime data;

	@Column(name = "referencia_externa_id", columnDefinition = "uuid", nullable = false)
	private UUID referenciaExternaId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Integer usuarioId) {
		this.usuarioId = usuarioId;
	}

	public TipoTransacao getTipo() {
		return tipo;
	}

	public void setTipo(TipoTransacao tipo) {
		this.tipo = tipo;
	}

	public DinheiroJpa getValor() {
		return valor;
	}

	public void setValor(DinheiroJpa valor) {
		this.valor = valor;
	}

	public LocalDateTime getData() {
		return data;
	}

	public void setData(LocalDateTime data) {
		this.data = data;
	}

	public UUID getReferenciaExternaId() {
		return referenciaExternaId;
	}

	public void setReferenciaExternaId(UUID referenciaExternaId) {
		this.referenciaExternaId = referenciaExternaId;
	}
}
