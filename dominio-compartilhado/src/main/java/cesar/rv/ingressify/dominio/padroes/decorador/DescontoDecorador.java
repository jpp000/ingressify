package cesar.rv.ingressify.dominio.padroes.decorador;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;

public class DescontoDecorador extends IngressoDecorador {

	private final int percentualDesconto;

	public DescontoDecorador(Ingresso decorado, int percentualDesconto) {
		super(decorado);
		Validate.isTrue(percentualDesconto > 0 && percentualDesconto < 100,
				"percentualDesconto deve estar entre 1 e 99");
		this.percentualDesconto = percentualDesconto;
	}

	@Override
	public Dinheiro obterPreco() {
		BigDecimal preco = decorado.obterPreco().getValor();
		BigDecimal desconto = preco
				.multiply(BigDecimal.valueOf(percentualDesconto))
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		return new Dinheiro(preco.subtract(desconto));
	}

	@Override
	public String obterDescricao() {
		return decorado.obterDescricao() + " (-" + percentualDesconto + "%)";
	}
}
