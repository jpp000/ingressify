package cesar.rv.ingressify.dominio.marketplace.denuncia;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

public final class DenunciaEventoId {

	private final Integer id;

	public DenunciaEventoId(Integer id) {
		Validate.notNull(id, "id");
		this.id = id;
	}

	public Integer getId() { return id; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DenunciaEventoId other)) return false;
		return id.equals(other.id);
	}

	@Override
	public int hashCode() { return Objects.hash(id); }
}
