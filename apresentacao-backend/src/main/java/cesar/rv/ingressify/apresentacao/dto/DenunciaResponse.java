package cesar.rv.ingressify.apresentacao.dto;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.denuncia.DecisaoModeracao;
import cesar.rv.ingressify.dominio.marketplace.denuncia.Denuncia;
import cesar.rv.ingressify.dominio.marketplace.denuncia.MotivoDenuncia;
import cesar.rv.ingressify.dominio.marketplace.denuncia.StatusDenuncia;

public record DenunciaResponse(
		int id,
		int anuncioId,
		int denuncianteId,
		MotivoDenuncia motivo,
		String descricao,
		StatusDenuncia status,
		DecisaoModeracao decisao,
		LocalDateTime criadaEm,
		LocalDateTime decididaEm) {

	public static DenunciaResponse fromDomain(Denuncia d) {
		return new DenunciaResponse(
				d.getId().getId(),
				d.getAnuncioId().getId(),
				d.getDenuncianteId().getId(),
				d.getMotivo(),
				d.getDescricao(),
				d.getStatus(),
				d.getDecisao(),
				d.getCriadaEm(),
				d.getDecididaEm());
	}
}
