package cesar.rv.ingressify.apresentacao.dto;

import java.time.LocalDateTime;

public record EditarEventoRequest(
		String nome,
		LocalDateTime dataHora,
		String local,
		String descricao,
		int capacidade,
		int capacidadeNumerada,
		String imagemCapaUrl,
		int prazoReembolsoDias,
		LocalDateTime aberturaPortoes,
		String categoria) {
}
