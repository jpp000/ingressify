package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;

public record TransacaoResponse(
		int id,
		int usuarioId,
		TipoTransacao tipo,
		BigDecimal valor,
		LocalDateTime data,
		UUID referenciaExternaId) {

	public static TransacaoResponse fromDomain(Transacao t) {
		return new TransacaoResponse(
				t.getId().getId(), t.getUsuario().getId(), t.getTipo(),
				t.getValor().getValor(), t.getData(), t.getReferenciaExternaId());
	}
}
