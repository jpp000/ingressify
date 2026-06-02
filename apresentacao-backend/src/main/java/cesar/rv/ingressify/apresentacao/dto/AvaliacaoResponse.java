package cesar.rv.ingressify.apresentacao.dto;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.avaliacao.Avaliacao;

public record AvaliacaoResponse(
		int id,
		int eventoId,
		int usuarioId,
		int nota,
		String comentario,
		String respostaOrganizador,
		LocalDateTime criadaEm) {

	public static AvaliacaoResponse fromDomain(Avaliacao a) {
		return new AvaliacaoResponse(
				a.getId().getId(), a.getEventoId().getId(), a.getUsuarioId().getId(),
				a.getNota(), a.getComentario(), a.getRespostaOrganizador(), a.getCriadaEm());
	}
}
