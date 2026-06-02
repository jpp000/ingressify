package cesar.rv.ingressify.apresentacao.dto;

import java.util.UUID;

import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckin;

public record ResultadoCheckInResponse(boolean sucesso, String mensagem, String ingressoId, UUID registroId) {

	public static ResultadoCheckInResponse fromDomain(RegistroCheckin r) {
		return new ResultadoCheckInResponse(true, "Check-in realizado com sucesso",
				r.getIngressoId().getId().toString(), r.getId());
	}
}
