package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;

public record ValidarCupomRequest(
		String codigo,
		int eventoId,
		BigDecimal valor) {
}
