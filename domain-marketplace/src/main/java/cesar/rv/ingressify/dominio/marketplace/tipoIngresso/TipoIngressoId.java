package cesar.rv.ingressify.dominio.marketplace.tipoIngresso;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public final class TipoIngressoId {

	private final int id;

	public TipoIngressoId(int id) {
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
		TipoIngressoId that = (TipoIngressoId) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
