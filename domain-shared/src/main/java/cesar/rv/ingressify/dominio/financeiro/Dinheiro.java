package cesar.rv.ingressify.dominio.financeiro;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

public final class Dinheiro {

	public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

	private final BigDecimal valor;

	public Dinheiro(BigDecimal valor) {
		Validate.notNull(valor, "valor");
		Validate.isTrue(valor.compareTo(BigDecimal.ZERO) >= 0, "valor deve ser >= 0");
		this.valor = valor.setScale(2, RoundingMode.HALF_UP);
	}

	public BigDecimal getValor() {
		return valor;
	}

	public Dinheiro somar(Dinheiro outro) {
		Validate.notNull(outro, "outro");
		return new Dinheiro(valor.add(outro.valor));
	}

	public Dinheiro subtrair(Dinheiro outro) {
		Validate.notNull(outro, "outro");
		return new Dinheiro(valor.subtract(outro.valor));
	}

	public Dinheiro multiplicar(int fator) {
		Validate.isTrue(fator >= 0, "fator deve ser >= 0");
		return new Dinheiro(valor.multiply(BigDecimal.valueOf(fator)));
	}

	public boolean maiorOuIgualA(Dinheiro outro) {
		Validate.notNull(outro, "outro");
		return valor.compareTo(outro.valor) >= 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Dinheiro dinheiro = (Dinheiro) o;
		return valor.compareTo(dinheiro.valor) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(valor.stripTrailingZeros());
	}

	@Override
	public String toString() {
		return valor.toPlainString();
	}
}
