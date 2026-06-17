package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.reembolso.MotivoReembolso;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolso;
import cesar.rv.ingressify.dominio.marketplace.reembolso.StatusSolicitacaoReembolso;

public record ReembolsoResponse(
		int id,
		String ingressoId,
		int solicitanteId,
		MotivoReembolso motivo,
		StatusSolicitacaoReembolso status,
		BigDecimal valor,
		LocalDateTime criadaEm,
		LocalDateTime decididaEm) {

	public static ReembolsoResponse fromDomain(SolicitacaoReembolso s) {
		return new ReembolsoResponse(
				s.getId().getId(),
				s.getIngressoId().getId().toString(),
				s.getSolicitanteId().getId(),
				s.getMotivo(),
				s.getStatus(),
				s.getValor().getValor(),
				s.getCriadaEm(),
				s.getDecididaEm());
	}
}
