package cesar.rv.ingressify.apresentacao.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckin;

public record CheckInItemResponse(UUID registroId, String ingressoId, int operadorId, LocalDateTime dataHora) {

	public static CheckInItemResponse fromDomain(RegistroCheckin r) {
		return new CheckInItemResponse(r.getId(), r.getIngressoId().getId().toString(),
				r.getOperadorId().getId(), r.getDataHora());
	}
}
