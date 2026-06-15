package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "tipo_ingresso_beneficios", joinColumns = @JoinColumn(name = "tipo_ingresso_id"))
	@Column(name = "beneficio")
	private List<String> beneficios = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "tipo_ingresso_id")
	@OrderBy("numero ASC")
	private List<LoteJpa> lotes = new ArrayList<>();

	@Column(name = "meia_entrada_habilitada", nullable = false, columnDefinition = "boolean default false")
	private boolean meiaEntradaHabilitada;

	@Column(name = "percentual_meia", nullable = false, columnDefinition = "integer default 0")
	private int percentualMeia;

	@Column(name = "cota_meia", nullable = false, columnDefinition = "integer default 0")
	private int cotaMeia;

	@Column(name = "cota_meia_disponivel", nullable = false, columnDefinition = "integer default 0")
	private int cotaMeiaDisponivel;

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
		jpa.beneficios = new ArrayList<>(t.getBeneficios());
		jpa.lotes = t.getLotes().stream().map(LoteJpa::fromDomain).collect(Collectors.toList());
		jpa.meiaEntradaHabilitada = t.isMeiaEntradaHabilitada();
		jpa.percentualMeia = t.getPercentualMeia();
		jpa.cotaMeia = t.getCotaMeia();
		jpa.cotaMeiaDisponivel = t.getCotaMeiaDisponivel();
		return jpa;
	}

	public TipoIngresso toDomain() {
		TipoIngresso t = new TipoIngresso(
				new TipoIngressoId(id), new EventoId(eventoId), nome,
				new Dinheiro(preco), quantidadeDisponivel, quantidadeTotal, descricao);
		t.definirBeneficios(new ArrayList<>(beneficios));
		for (LoteJpa l : lotes) {
			t.adicionarLote(l.toDomain());
		}
		t.reconstituirMeiaEntrada(meiaEntradaHabilitada, percentualMeia, cotaMeia, cotaMeiaDisponivel);
		return t;
	}

	public Integer getId() { return id; }
	public Integer getEventoId() { return eventoId; }
	public List<LoteJpa> getLotes() { return lotes; }
}
