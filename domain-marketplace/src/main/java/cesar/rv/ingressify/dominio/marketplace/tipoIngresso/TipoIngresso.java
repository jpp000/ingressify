package cesar.rv.ingressify.dominio.marketplace.tipoIngresso;

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

	public TipoIngresso(EventoId eventoId, String nome, Dinheiro preco, int quantidadeDisponivel, int quantidadeTotal) {
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
	}

	public TipoIngresso(TipoIngressoId id, EventoId eventoId, String nome, Dinheiro preco, int quantidadeDisponivel,
			int quantidadeTotal) {
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
	}

	public void atribuirId(TipoIngressoId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void reservar(int qtd) {
		Validate.isTrue(qtd > 0, "qtd deve ser > 0");
		if (quantidadeDisponivel < qtd) {
			throw new IllegalStateException("quantidade indisponível");
		}
		this.quantidadeDisponivel -= qtd;
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
}
