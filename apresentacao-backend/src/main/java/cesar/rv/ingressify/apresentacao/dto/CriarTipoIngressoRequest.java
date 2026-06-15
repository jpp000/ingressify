package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;
import java.util.List;

public record CriarTipoIngressoRequest(
		String nome,
		BigDecimal preco,
		int quantidade,
		String descricao,
		List<String> beneficios,
		List<CriarLoteRequest> lotes,
		boolean meiaEntradaHabilitada,
		int percentualMeia,
		int cotaMeia) {
}
