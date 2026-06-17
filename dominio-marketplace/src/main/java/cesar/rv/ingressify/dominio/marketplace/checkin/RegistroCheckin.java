package cesar.rv.ingressify.dominio.marketplace.checkin;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

public class RegistroCheckin {

	private final UUID id;
	private final IngressoId ingressoId;
	private final EventoId eventoId;
	private final UsuarioId operadorId;
	private final LocalDateTime dataHora;

	public RegistroCheckin(UUID id, IngressoId ingressoId, EventoId eventoId, UsuarioId operadorId,
			LocalDateTime dataHora) {
		Validate.notNull(id, "id");
		Validate.notNull(ingressoId, "ingressoId");
		Validate.notNull(eventoId, "eventoId");
		Validate.notNull(operadorId, "operadorId");
		Validate.notNull(dataHora, "dataHora");
		this.id = id;
		this.ingressoId = ingressoId;
		this.eventoId = eventoId;
		this.operadorId = operadorId;
		this.dataHora = dataHora;
	}

	public UUID getId() {
		return id;
	}

	public IngressoId getIngressoId() {
		return ingressoId;
	}

	public EventoId getEventoId() {
		return eventoId;
	}

	public UsuarioId getOperadorId() {
		return operadorId;
	}

	public LocalDateTime getDataHora() {
		return dataHora;
	}
}
