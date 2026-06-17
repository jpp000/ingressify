package cesar.rv.ingressify.dominio.marketplace.denuncia;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public final class DenunciaId {

	private final int id;

	public DenunciaId(int id) {
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
		DenunciaId denunciaId = (DenunciaId) o;
		return id == denunciaId.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
