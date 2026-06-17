package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;

public record ValidarCupomResponse(
		String codigo,
		BigDecimal valorOriginal,
		BigDecimal desconto,
		BigDecimal valorComDesconto) {
}
