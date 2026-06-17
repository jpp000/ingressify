package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;

import cesar.rv.ingressify.aplicacao.financeiro.carteira.ResultadoResgate;

public record ResgatePontosResponse(
		int pontos,
		BigDecimal valor,
		int pontosResgatados,
		BigDecimal valorCreditado) {

	public static ResgatePontosResponse fromResultado(ResultadoResgate r) {
		return new ResgatePontosResponse(
				r.pontosRestantes(),
				r.saldo().getValor().getValor(),
				r.pontosResgatados(),
				r.valorCreditado().getValor());
	}
}
