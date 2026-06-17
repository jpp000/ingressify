package cesar.rv.ingressify.aplicacao.financeiro.carteira;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.saldo.Saldo;

/**
 * Resultado de um resgate de pontos: saldo atualizado, pontos que sobraram,
 * quantos pontos foram resgatados e o valor creditado na carteira.
 */
public record ResultadoResgate(Saldo saldo, int pontosRestantes, int pontosResgatados, Dinheiro valorCreditado) {
}
