package cesar.rv.ingressify.apresentacao.marketplace.evento;

import java.time.LocalDateTime;

public class EventoFormulario {

	public record EventoDto(String nome, LocalDateTime dataHora, String local, int capacidade) {
	}

	private EventoDto evento;

	public EventoDto getEvento() {
		return evento;
	}

	public void setEvento(EventoDto evento) {
		this.evento = evento;
	}
}
