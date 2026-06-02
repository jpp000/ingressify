package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

@Entity
@Table(name = "tipos_ingresso")
public class TipoIngressoJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "evento_id", nullable = false)
	private Integer eventoId;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal preco;

	@Column(name = "quantidade_disponivel", nullable = false)
	private int quantidadeDisponivel;

	@Column(name = "quantidade_total", nullable = false)
	private int quantidadeTotal;

	@Column(columnDefinition = "TEXT")
	private String descricao;

	protected TipoIngressoJpa() {}

	public static TipoIngressoJpa fromDomain(TipoIngresso t) {
		var jpa = new TipoIngressoJpa();
		if (t.getId() != null) jpa.id = t.getId().getId();
		jpa.eventoId = t.getEventoId().getId();
		jpa.nome = t.getNome();
		jpa.preco = t.getPreco().getValor();
		jpa.quantidadeDisponivel = t.getQuantidadeDisponivel();
		jpa.quantidadeTotal = t.getQuantidadeTotal();
		jpa.descricao = t.getDescricao();
		return jpa;
	}

	public TipoIngresso toDomain() {
		return new TipoIngresso(
				new TipoIngressoId(id), new EventoId(eventoId), nome,
				new Dinheiro(preco), quantidadeDisponivel, quantidadeTotal, descricao);
	}

	public Integer getId() { return id; }
	public Integer getEventoId() { return eventoId; }
}
