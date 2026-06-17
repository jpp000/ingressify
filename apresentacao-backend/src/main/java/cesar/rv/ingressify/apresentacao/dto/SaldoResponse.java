package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;

import cesar.rv.ingressify.dominio.financeiro.saldo.Saldo;

public record SaldoResponse(int usuarioId, BigDecimal valor) {

	public static SaldoResponse fromDomain(Saldo s) {
		return new SaldoResponse(s.getUsuario().getId(), s.getValor().getValor());
	}
}
