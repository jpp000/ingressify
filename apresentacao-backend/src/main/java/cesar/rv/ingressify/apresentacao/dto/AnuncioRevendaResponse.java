package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;
import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;

public record AnuncioRevendaResponse(
		int id,
		List<String> ingressoIds,
		int vendedorId,
		BigDecimal preco,
		StatusAnuncio status,
		int quantidade) {

	public static AnuncioRevendaResponse fromDomain(AnuncioRevenda a) {
		return new AnuncioRevendaResponse(
				a.getId().getId(),
				a.getIngressoIds().stream().map(i -> i.getId().toString()).toList(),
				a.getVendedor().getId(),
				a.getPreco().getValor(),
				a.getStatus(),
				a.getQuantidade());
	}
}
