package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;
import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;

public record TipoIngressoResponse(
		int id,
		String nome,
		BigDecimal preco,
		int quantidadeDisponivel,
		int quantidadeTotal,
		String descricao,
		List<String> beneficios,
		List<LoteResponse> lotes) {

	public static TipoIngressoResponse fromDomain(TipoIngresso t) {
		return new TipoIngressoResponse(
				t.getId().getId(), t.getNome(), t.getPreco().getValor(),
				t.getQuantidadeDisponivel(), t.getQuantidadeTotal(), t.getDescricao(),
				t.getBeneficios(),
				t.getLotes().stream().map(LoteResponse::fromDomain).toList());
	}
}
