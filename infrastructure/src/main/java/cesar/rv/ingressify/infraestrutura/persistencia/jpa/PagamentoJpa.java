package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.pagamento.StatusPagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;
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
@Table(name = "pagamento")
public class PagamentoJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Embedded
	private DinheiroJpa valor = new DinheiroJpa();

	@Column(name = "comprador_id", nullable = false)
	private Integer compradorId;

	@Column(name = "vendedor_id")
	private Integer vendedorId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TipoOperacao tipo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private StatusPagamento status;

	@Column(name = "correlacao_id", columnDefinition = "uuid", nullable = false)
	private UUID correlacaoId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public DinheiroJpa getValor() {
		return valor;
	}

	public void setValor(DinheiroJpa valor) {
		this.valor = valor;
	}

	public Integer getCompradorId() {
		return compradorId;
	}

	public void setCompradorId(Integer compradorId) {
		this.compradorId = compradorId;
	}

	public Integer getVendedorId() {
		return vendedorId;
	}

	public void setVendedorId(Integer vendedorId) {
		this.vendedorId = vendedorId;
	}

	public TipoOperacao getTipo() {
		return tipo;
	}

	public void setTipo(TipoOperacao tipo) {
		this.tipo = tipo;
	}

	public StatusPagamento getStatus() {
		return status;
	}

	public void setStatus(StatusPagamento status) {
		this.status = status;
	}

	public UUID getCorrelacaoId() {
		return correlacaoId;
	}

	public void setCorrelacaoId(UUID correlacaoId) {
		this.correlacaoId = correlacaoId;
	}
}
