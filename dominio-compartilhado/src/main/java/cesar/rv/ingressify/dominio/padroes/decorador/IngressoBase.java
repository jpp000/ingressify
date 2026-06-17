package cesar.rv.ingressify.dominio.padroes.decorador;

import java.math.BigDecimal;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;

public class IngressoBase implements Ingresso {

	private final Dinheiro preco;
	private final String descricao;

	public IngressoBase(Dinheiro preco, String descricao) {
		Validate.notNull(preco, "preco");
		Validate.notBlank(descricao, "descricao");
		this.preco = preco;
		this.descricao = descricao;
	}

	public IngressoBase(BigDecimal preco, String descricao) {
		this(new Dinheiro(preco), descricao);
	}

	public IngressoBase(int preco) {
		this(new Dinheiro(BigDecimal.valueOf(preco)), "Ingresso");
	}

	@Override
	public Dinheiro obterPreco() {
		return preco;
	}

	@Override
	public String obterDescricao() {
		return descricao;
	}
}
