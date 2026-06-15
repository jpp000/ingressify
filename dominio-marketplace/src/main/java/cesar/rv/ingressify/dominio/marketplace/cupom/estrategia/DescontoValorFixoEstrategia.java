package cesar.rv.ingressify.dominio.marketplace.cupom.estrategia;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;

/**
 * Desconto de valor fixo. Nunca deixa o total negativo (piso em zero).
 */
public class DescontoValorFixoEstrategia implements EstrategiaDesconto {

	private final Dinheiro desconto;

	public DescontoValorFixoEstrategia(Dinheiro desconto) {
		Validate.notNull(desconto, "desconto");
		this.desconto = desconto;
	}

	@Override
	public Dinheiro aplicar(Dinheiro valorOriginal) {
		Validate.notNull(valorOriginal, "valorOriginal");
		if (desconto.maiorOuIgualA(valorOriginal)) {
			return Dinheiro.ZERO;
		}
		return valorOriginal.subtrair(desconto);
	}
}
