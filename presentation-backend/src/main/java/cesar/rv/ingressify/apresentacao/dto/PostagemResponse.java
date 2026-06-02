package cesar.rv.ingressify.apresentacao.dto;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.feed.Postagem;

public record PostagemResponse(
		int id,
		int eventoId,
		int autorId,
		String titulo,
		String conteudo,
		String imagemUrl,
		boolean fixada,
		LocalDateTime criadaEm) {

	public static PostagemResponse fromDomain(Postagem p) {
		return new PostagemResponse(
				p.getId().getId(), p.getEventoId().getId(), p.getAutorId().getId(),
				p.getTitulo(), p.getConteudo(), p.getImagemUrl(), p.isFixada(), p.getCriadaEm());
	}
}
