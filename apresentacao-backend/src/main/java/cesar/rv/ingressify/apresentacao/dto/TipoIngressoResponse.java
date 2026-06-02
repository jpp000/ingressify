package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;

import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;

public record TipoIngressoResponse(
		int id,
		String nome,
		BigDecimal preco,
		int quantidadeDisponivel,
		int quantidadeTotal,
		String descricao) {

	public static TipoIngressoResponse fromDomain(TipoIngresso t) {
		return new TipoIngressoResponse(
				t.getId().getId(), t.getNome(), t.getPreco().getValor(),
				t.getQuantidadeDisponivel(), t.getQuantidadeTotal(), t.getDescricao());
	}
}
