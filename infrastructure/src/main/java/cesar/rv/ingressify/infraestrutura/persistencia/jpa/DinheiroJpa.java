package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DinheiroJpa {

	@Column(name = "valor", nullable = false, precision = 14, scale = 2)
	private BigDecimal valor = BigDecimal.ZERO;

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
}
