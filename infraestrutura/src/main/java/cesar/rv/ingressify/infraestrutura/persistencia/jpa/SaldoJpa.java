package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.saldo.Saldo;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

@Entity
@Table(name = "saldos")
public class SaldoJpa {

	@Id
	@Column(name = "usuario_id")
	private Integer usuarioId;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal valor;

	protected SaldoJpa() {}

	public static SaldoJpa fromDomain(Saldo s) {
		var jpa = new SaldoJpa();
		jpa.usuarioId = s.getUsuario().getId();
		jpa.valor = s.getValor().getValor();
		return jpa;
	}

	public Saldo toDomain() {
		return new Saldo(new UsuarioId(usuarioId), new Dinheiro(valor));
	}

	public Integer getUsuarioId() { return usuarioId; }
}
