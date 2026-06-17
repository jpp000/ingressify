package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cesar.rv.ingressify.aplicacao.marketplace.catalogo.EventoCatalogoResumo;

public record EventoCatalogoResponse(
		int id,
		String nome,
		LocalDateTime dataHora,
		String local,
		String imagemCapaUrl,
		BigDecimal precoMinimo,
		double mediaAvaliacao,
		boolean temEstoquePrimario,
		boolean temRevendaDisponivel,
		String categoria) {

	public static EventoCatalogoResponse fromResumo(EventoCatalogoResumo r) {
		return new EventoCatalogoResponse(
				r.getId().getId(),
				r.getNome(),
				r.getDataHora(),
				r.getLocal(),
				r.getImagemCapaUrl(),
				r.getPrecoMinimo() != null ? r.getPrecoMinimo().getValor() : null,
				r.getMediaAvaliacao(),
				r.isTemEstoquePrimario(),
				r.isTemRevendaDisponivel(),
				r.getCategoria());
	}
}
