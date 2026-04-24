package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "saldo")
public class SaldoJpa {

	@Id
	@Column(name = "usuario_id", nullable = false)
	private Integer usuarioId;

	@Embedded
	private DinheiroJpa valor = new DinheiroJpa();

	public Integer getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Integer usuarioId) {
		this.usuarioId = usuarioId;
	}

	public DinheiroJpa getValor() {
		return valor;
	}

	public void setValor(DinheiroJpa valor) {
		this.valor = valor;
	}
}
