package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CriarLoteRequest(
		String nome,
		BigDecimal preco,
		int quantidade,
		LocalDateTime dataInicio,
		LocalDateTime dataFim) {
}
