package cesar.rv.ingressify.dominio.marketplace.cupom.estrategia;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.padroes.decorador.DescontoDecorador;
import cesar.rv.ingressify.dominio.padroes.decorador.IngressoBase;

/**
 * Desconto percentual. Reaproveita o {@link DescontoDecorador} (padrão Decorator)
 * já existente no domínio para calcular o preço com desconto.
 */
public class DescontoPercentualEstrategia implements EstrategiaDesconto {

	private final int percentual;

	public DescontoPercentualEstrategia(int percentual) {
		Validate.isTrue(percentual > 0 && percentual < 100, "percentual deve estar entre 1 e 99");
		this.percentual = percentual;
	}

	@Override
	public Dinheiro aplicar(Dinheiro valorOriginal) {
		Validate.notNull(valorOriginal, "valorOriginal");
		return new DescontoDecorador(new IngressoBase(valorOriginal, "Pedido"), percentual).obterPreco();
	}
}
