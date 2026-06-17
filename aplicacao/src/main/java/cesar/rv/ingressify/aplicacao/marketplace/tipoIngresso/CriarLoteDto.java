package cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CriarLoteDto(
		String nome,
		BigDecimal preco,
		int quantidade,
		LocalDateTime dataInicio,
		LocalDateTime dataFim) {
}
