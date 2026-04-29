package cesar.rv.ingressify.dominio.marketplace;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;

public final class MarketplaceTestData {

	private MarketplaceTestData() {
	}

	public static UsuarioId organizadorPadrao() {
		return new UsuarioId(1);
	}

	public static LocalDateTime aberturaPara(LocalDateTime dataHoraEvento) {
		return dataHoraEvento.minusHours(2);
	}

	public static Evento eventoNovo(String nome, LocalDateTime dataHora, String local, String descricao, int capacidade) {
		return new Evento(organizadorPadrao(), nome, dataHora, local, descricao, capacidade, null, 7,
				aberturaPara(dataHora));
	}

	public static Evento eventoReidratado(EventoId id, String nome, LocalDateTime dataHora, String local,
			String descricao, StatusEvento status, int capacidade) {
		return new Evento(id, organizadorPadrao(), nome, dataHora, local, descricao, status, capacidade, null, 7,
				aberturaPara(dataHora));
	}
}
