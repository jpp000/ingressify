package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.Lote;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.LoteId;

@Entity
@Table(name = "lotes")
public class LoteJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private int numero;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal preco;

	@Column(name = "quantidade_total", nullable = false)
	private int quantidadeTotal;

	@Column(name = "quantidade_disponivel", nullable = false)
	private int quantidadeDisponivel;

	@Column(name = "data_inicio")
	private LocalDateTime dataInicio;

	@Column(name = "data_fim")
	private LocalDateTime dataFim;

	protected LoteJpa() {}

	public static LoteJpa fromDomain(Lote lote) {
		var jpa = new LoteJpa();
		if (lote.getId() != null) jpa.id = lote.getId().getId();
		jpa.numero = lote.getNumero();
		jpa.nome = lote.getNome();
		jpa.preco = lote.getPreco().getValor();
		jpa.quantidadeTotal = lote.getQuantidadeTotal();
		jpa.quantidadeDisponivel = lote.getQuantidadeDisponivel();
		jpa.dataInicio = lote.getDataInicio();
		jpa.dataFim = lote.getDataFim();
		return jpa;
	}

	public Lote toDomain() {
		return new Lote(
				new LoteId(id), numero, nome, new Dinheiro(preco),
				quantidadeTotal, quantidadeDisponivel, dataInicio, dataFim);
	}

	public Integer getId() { return id; }
	public int getNumero() { return numero; }
	public int getQuantidadeTotal() { return quantidadeTotal; }
	public int getQuantidadeDisponivel() { return quantidadeDisponivel; }
	public BigDecimal getPreco() { return preco; }
}
