package cesar.rv.ingressify.apresentacao.dto;

public record EventoAnalyticsResponse(
		int totalVendidos,
		int totalRevendidos,
		int totalDisponiveis,
		int totalCapacidade,
		double taxaOcupacao,
		double taxaRevenda,
		double mediaAvaliacao,
		int totalAvaliacoes) {
}
