package cesar.rv.ingressify.dominio.marketplace.evento;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public final class EventoId {

	private final int id;

	public EventoId(int id) {
		Validate.isTrue(id > 0, "id deve ser > 0");
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		EventoId eventoId = (EventoId) o;
		return id == eventoId.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
