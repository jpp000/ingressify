package cesar.rv.ingressify.aplicacao.marketplace.analytics;

public record EventoAnalyticsDto(
		int totalVendidos,
		int totalRevendidos,
		int totalDisponiveis,
		int totalCapacidade,
		double taxaOcupacao,
		double taxaRevenda,
		double mediaAvaliacao,
		int totalAvaliacoes) {
}
