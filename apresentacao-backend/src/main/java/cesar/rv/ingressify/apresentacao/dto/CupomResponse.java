package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.cupom.Cupom;
import cesar.rv.ingressify.dominio.marketplace.cupom.TipoCupom;

public record CupomResponse(
		int id,
		String codigo,
		TipoCupom tipo,
		BigDecimal valor,
		int eventoId,
		BigDecimal valorMinimo,
		int limiteUsos,
		int usos,
		LocalDateTime validoDe,
		LocalDateTime validoAte,
		boolean ativo) {

	public static CupomResponse fromDomain(Cupom c) {
		return new CupomResponse(
				c.getId().getId(), c.getCodigo(), c.getTipo(), c.getValor(), c.getEventoId().getId(),
				c.getValorMinimo().getValor(), c.getLimiteUsos(), c.getUsos(),
				c.getValidoDe(), c.getValidoAte(), c.isAtivo());
	}
}
