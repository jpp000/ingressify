package cesar.rv.ingressify.dominio.marketplace.tipoIngresso;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;

public class Lote {

	private LoteId id;
	private int numero;
	private String nome;
	private Dinheiro preco;
	private int quantidadeTotal;
	private int quantidadeDisponivel;
	private LocalDateTime dataInicio;
	private LocalDateTime dataFim;

	public Lote(int numero, String nome, Dinheiro preco, int quantidadeTotal,
			LocalDateTime dataInicio, LocalDateTime dataFim) {
		Validate.notBlank(nome, "nome");
		Validate.notNull(preco, "preco");
		Validate.isTrue(quantidadeTotal > 0, "quantidadeTotal deve ser > 0");
		this.numero = numero;
		this.nome = nome;
		this.preco = preco;
		this.quantidadeTotal = quantidadeTotal;
		this.quantidadeDisponivel = quantidadeTotal;
		this.dataInicio = dataInicio;
		this.dataFim = dataFim;
	}

	public Lote(LoteId id, int numero, String nome, Dinheiro preco, int quantidadeTotal,
			int quantidadeDisponivel, LocalDateTime dataInicio, LocalDateTime dataFim) {
		Validate.notNull(id, "id");
		Validate.notBlank(nome, "nome");
		Validate.notNull(preco, "preco");
		Validate.isTrue(quantidadeTotal > 0, "quantidadeTotal deve ser > 0");
		this.id = id;
		this.numero = numero;
		this.nome = nome;
		this.preco = preco;
		this.quantidadeTotal = quantidadeTotal;
		this.quantidadeDisponivel = quantidadeDisponivel;
		this.dataInicio = dataInicio;
		this.dataFim = dataFim;
	}

	public void atribuirId(LoteId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public boolean ativo(LocalDateTime agora) {
		if (quantidadeDisponivel <= 0) return false;
		if (dataInicio != null && agora.isBefore(dataInicio)) return false;
		if (dataFim != null && agora.isAfter(dataFim)) return false;
		return true;
	}

	public void reservar(int qtd) {
		Validate.isTrue(qtd > 0, "qtd deve ser > 0");
		if (quantidadeDisponivel < qtd) {
			throw new IllegalStateException("quantidade indisponível no lote");
		}
		this.quantidadeDisponivel -= qtd;
	}

	public void devolver(int qtd) {
		Validate.isTrue(qtd > 0, "qtd deve ser > 0");
		int novo = quantidadeDisponivel + qtd;
		Validate.isTrue(novo <= quantidadeTotal, "devolução excede total do lote");
		this.quantidadeDisponivel = novo;
	}

	public LoteId getId() { return id; }
	public int getNumero() { return numero; }
	public String getNome() { return nome; }
	public Dinheiro getPreco() { return preco; }
	public int getQuantidadeTotal() { return quantidadeTotal; }
	public int getQuantidadeDisponivel() { return quantidadeDisponivel; }
	public LocalDateTime getDataInicio() { return dataInicio; }
	public LocalDateTime getDataFim() { return dataFim; }
}
