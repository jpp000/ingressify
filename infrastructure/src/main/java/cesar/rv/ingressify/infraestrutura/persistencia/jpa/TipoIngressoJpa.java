package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_ingresso")
public class TipoIngressoJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "evento_id", nullable = false)
	private Integer eventoId;

	@Column(nullable = false, length = 255)
	private String nome;

	@Embedded
	private DinheiroJpa preco = new DinheiroJpa();

	@Column(name = "quantidade_disponivel", nullable = false)
	private int quantidadeDisponivel;

	@Column(name = "quantidade_total", nullable = false)
	private int quantidadeTotal;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEventoId() {
		return eventoId;
	}

	public void setEventoId(Integer eventoId) {
		this.eventoId = eventoId;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public DinheiroJpa getPreco() {
		return preco;
	}

	public void setPreco(DinheiroJpa preco) {
		this.preco = preco;
	}

	public int getQuantidadeDisponivel() {
		return quantidadeDisponivel;
	}

	public void setQuantidadeDisponivel(int quantidadeDisponivel) {
		this.quantidadeDisponivel = quantidadeDisponivel;
	}

	public int getQuantidadeTotal() {
		return quantidadeTotal;
	}

	public void setQuantidadeTotal(int quantidadeTotal) {
		this.quantidadeTotal = quantidadeTotal;
	}
}
