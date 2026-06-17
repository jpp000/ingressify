package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.cupom.TipoCupom;

public record CriarCupomRequest(
		String codigo,
		TipoCupom tipo,
		BigDecimal valor,
		BigDecimal valorMinimo,
		int limiteUsos,
		LocalDateTime validoDe,
		LocalDateTime validoAte) {
}
