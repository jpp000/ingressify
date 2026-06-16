package cesar.rv.ingressify.dominio.marketplace.grupoCompra;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public final class GrupoCompraId {

	private final int id;

	public GrupoCompraId(int id) {
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
		GrupoCompraId that = (GrupoCompraId) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "GrupoCompraId(" + id + ")";
	}
}
