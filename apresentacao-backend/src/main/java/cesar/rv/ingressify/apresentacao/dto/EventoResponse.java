package cesar.rv.ingressify.apresentacao.dto;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;

public record EventoResponse(
		int id,
		String nome,
		LocalDateTime dataHora,
		String local,
		String descricao,
		StatusEvento status,
		int capacidade,
		String imagemCapaUrl,
		int prazoReembolsoDias,
		LocalDateTime aberturaPortoes,
		String categoria) {

	public static EventoResponse fromDomain(Evento e) {
		return new EventoResponse(
				e.getId().getId(), e.getNome(), e.getDataHora(), e.getLocal(),
				e.getDescricao(), e.getStatus(), e.getCapacidade(),
				e.getImagemCapaUrl(), e.getPrazoReembolsoDias(), e.getAberturaPortoes(),
				e.getCategoria());
	}
}
