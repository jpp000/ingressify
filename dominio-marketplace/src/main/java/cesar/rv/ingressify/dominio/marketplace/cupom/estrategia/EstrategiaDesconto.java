package cesar.rv.ingressify.dominio.marketplace.cupom.estrategia;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;

/**
 * Strategy para o cálculo do desconto de um cupom sobre o valor de um pedido.
 */
public interface EstrategiaDesconto {

	Dinheiro aplicar(Dinheiro valorOriginal);
}
