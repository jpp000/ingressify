package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;

public record CriarTipoIngressoRequest(
		String nome,
		BigDecimal preco,
		int quantidade,
		String descricao) {
}
