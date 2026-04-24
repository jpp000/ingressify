package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "compra_pendente")
public class CompraPendenteJpa {

	@Id
	@Column(columnDefinition = "uuid", nullable = false)
	private UUID id;

	@Column(name = "tipo_ingresso_id", nullable = false)
	private Integer tipoIngressoId;

	@Column(name = "evento_id", nullable = false)
	private Integer eventoId;

	@Column(nullable = false)
	private int quantidade;

	@Column(name = "comprador_id", nullable = false)
	private Integer compradorId;

	@Embedded
	private DinheiroJpa valorTotal = new DinheiroJpa();

	@Column(name = "criada_em", nullable = false)
	private LocalDateTime criadaEm;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Integer getTipoIngressoId() {
		return tipoIngressoId;
	}

	public void setTipoIngressoId(Integer tipoIngressoId) {
		this.tipoIngressoId = tipoIngressoId;
	}

	public Integer getEventoId() {
		return eventoId;
	}

	public void setEventoId(Integer eventoId) {
		this.eventoId = eventoId;
	}

	public int getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(int quantidade) {
		this.quantidade = quantidade;
	}

	public Integer getCompradorId() {
		return compradorId;
	}

	public void setCompradorId(Integer compradorId) {
		this.compradorId = compradorId;
	}

	public DinheiroJpa getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(DinheiroJpa valorTotal) {
		this.valorTotal = valorTotal;
	}

	public LocalDateTime getCriadaEm() {
		return criadaEm;
	}

	public void setCriadaEm(LocalDateTime criadaEm) {
		this.criadaEm = criadaEm;
	}
}
