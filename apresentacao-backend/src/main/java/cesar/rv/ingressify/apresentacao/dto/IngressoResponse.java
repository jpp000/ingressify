package cesar.rv.ingressify.apresentacao.dto;

import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;

public record IngressoResponse(
		String id,
		int tipoIngressoId,
		int eventoId,
		int proprietarioId,
		StatusIngresso status,
		boolean bloqueadoPorReembolso,
		boolean meiaEntrada,
		String documento) {

	public static IngressoResponse fromDomain(Ingresso i) {
		return new IngressoResponse(
				i.getId().getId().toString(),
				i.getTipoIngressoId().getId(),
				i.getEventoId().getId(),
				i.getProprietario().getId(),
				i.getStatus(),
				i.isBloqueadoPorReembolso(),
				i.isMeiaEntrada(),
				i.getDocumento());
	}
}
