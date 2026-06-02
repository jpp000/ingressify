package cesar.rv.ingressify.dominio.identidade;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public final class UsuarioId {

	private final int id;

	public UsuarioId(int id) {
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
		UsuarioId usuarioId = (UsuarioId) o;
		return id == usuarioId.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}
}
