package cesar.rv.ingressify.dominio.marketplace.ingresso;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

public final class IngressoId {

	private final UUID id;

	public IngressoId(UUID id) {
		Validate.notNull(id, "id");
		this.id = id;
	}

	public UUID getId() {
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
		IngressoId that = (IngressoId) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return id.toString();
	}
}
