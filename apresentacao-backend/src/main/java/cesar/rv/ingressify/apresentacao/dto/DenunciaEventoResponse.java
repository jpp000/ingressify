package cesar.rv.ingressify.apresentacao.dto;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEvento;
import cesar.rv.ingressify.dominio.marketplace.denuncia.MotivoDenunciaEvento;
import cesar.rv.ingressify.dominio.marketplace.denuncia.StatusDenunciaEvento;

public record DenunciaEventoResponse(
		int id,
		int eventoId,
		int denuncianteId,
		MotivoDenunciaEvento motivo,
		String descricao,
		StatusDenunciaEvento status,
		LocalDateTime criadaEm,
		LocalDateTime decididaEm) {

	public static DenunciaEventoResponse fromDomain(DenunciaEvento d) {
		return new DenunciaEventoResponse(
				d.getId().getId(),
				d.getEventoId().getId(),
				d.getDenuncianteId().getId(),
				d.getMotivo(),
				d.getDescricao(),
				d.getStatus(),
				d.getCriadaEm(),
				d.getDecididaEm());
	}
}
