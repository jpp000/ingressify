package cesar.rv.ingressify.dominio.marketplace.tipoIngresso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public class TipoIngresso {

	private TipoIngressoId id;
	private EventoId eventoId;
	private String nome;
	private Dinheiro preco;
	private int quantidadeDisponivel;
	private int quantidadeTotal;
	private String descricao;
	private List<String> beneficios = new ArrayList<>();
	private List<Lote> lotes = new ArrayList<>();

	public TipoIngresso(EventoId eventoId, String nome, Dinheiro preco, int quantidadeDisponivel, int quantidadeTotal,
			String descricao) {
		Validate.notNull(eventoId, "eventoId");
		Validate.notBlank(nome, "nome");
		Validate.notNull(preco, "preco");
		Validate.isTrue(quantidadeTotal > 0, "quantidadeTotal deve ser > 0");
		Validate.isTrue(quantidadeDisponivel >= 0 && quantidadeDisponivel <= quantidadeTotal,
				"quantidadeDisponivel inválida");
		this.eventoId = eventoId;
		this.nome = nome;
		this.preco = preco;
		this.quantidadeDisponivel = quantidadeDisponivel;
		this.quantidadeTotal = quantidadeTotal;
		this.descricao = descricao;
	}

	public TipoIngresso(TipoIngressoId id, EventoId eventoId, String nome, Dinheiro preco, int quantidadeDisponivel,
			int quantidadeTotal, String descricao) {
		Validate.notNull(id, "id");
		Validate.notNull(eventoId, "eventoId");
		Validate.notBlank(nome, "nome");
		Validate.notNull(preco, "preco");
		Validate.isTrue(quantidadeTotal > 0, "quantidadeTotal deve ser > 0");
		Validate.isTrue(quantidadeDisponivel >= 0 && quantidadeDisponivel <= quantidadeTotal,
				"quantidadeDisponivel inválida");
		this.id = id;
		this.eventoId = eventoId;
		this.nome = nome;
		this.preco = preco;
		this.quantidadeDisponivel = quantidadeDisponivel;
		this.quantidadeTotal = quantidadeTotal;
		this.descricao = descricao;
	}

	public void atribuirId(TipoIngressoId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void atualizar(String nome, Dinheiro preco, int novaQuantidadeTotal, String descricao) {
		Validate.notBlank(nome, "nome");
		Validate.notNull(preco, "preco");
		int vendidos = quantidadeTotal - quantidadeDisponivel;
		if (novaQuantidadeTotal < vendidos) {
			throw new IllegalStateException("nova quantidade total menor que ingressos já vendidos");
		}
		this.nome = nome;
		this.preco = preco;
		this.quantidadeTotal = novaQuantidadeTotal;
		this.quantidadeDisponivel = novaQuantidadeTotal - vendidos;
		this.descricao = descricao;
	}

	public void adicionarLote(Lote lote) {
		Validate.notNull(lote, "lote");
		lotes.add(lote);
	}

	public void definirBeneficios(List<String> novos) {
		this.beneficios = novos != null ? new ArrayList<>(novos) : new ArrayList<>();
	}

	public Optional<Lote> loteAtivo() {
		LocalDateTime agora = LocalDateTime.now();
		return lotes.stream()
				.filter(l -> l.ativo(agora))
				.min(Comparator.comparingInt(Lote::getNumero));
	}

	public void reservar(int qtd) {
		Validate.isTrue(qtd > 0, "qtd deve ser > 0");
		if (quantidadeDisponivel < qtd) {
			throw new IllegalStateException("quantidade indisponível");
		}
		this.quantidadeDisponivel -= qtd;
		if (!lotes.isEmpty()) {
			loteAtivo()
					.orElseThrow(() -> new IllegalStateException("nenhum lote ativo disponível"))
					.reservar(qtd);
		}
	}

	public void devolver(int qtd) {
		Validate.isTrue(qtd > 0, "qtd deve ser > 0");
		int novo = quantidadeDisponivel + qtd;
		Validate.isTrue(novo <= quantidadeTotal, "devolução excede total");
		this.quantidadeDisponivel = novo;
	}

	public TipoIngressoId getId() {
		return id;
	}

	public EventoId getEventoId() {
		return eventoId;
	}

	public String getNome() {
		return nome;
	}

	public Dinheiro getPreco() {
		return preco;
	}

	public int getQuantidadeDisponivel() {
		return quantidadeDisponivel;
	}

	public int getQuantidadeTotal() {
		return quantidadeTotal;
	}

	public String getDescricao() {
		return descricao;
	}

	public List<String> getBeneficios() {
		return Collections.unmodifiableList(beneficios);
	}

	public List<Lote> getLotes() {
		return Collections.unmodifiableList(lotes);
	}
}
