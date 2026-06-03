package cesar.rv.ingressify.dominio.marketplace.tipoIngresso;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public final class LoteId {

	private final int id;

	public LoteId(int id) {
		Validate.isTrue(id > 0, "id deve ser > 0");
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LoteId)) return false;
		return id == ((LoteId) o).id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
