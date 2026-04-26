package cesar.rv.ingressify.dominio.financeiro.saldo;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class Saldo {

	private UsuarioId usuario;
	private Dinheiro valor;

	public Saldo(UsuarioId usuario, Dinheiro valor) {
		Validate.notNull(usuario, "usuario");
		Validate.notNull(valor, "valor");
		this.usuario = usuario;
		this.valor = valor;
	}

	public void creditar(Dinheiro quantia) {
		Validate.notNull(quantia, "quantia");
		this.valor = this.valor.somar(quantia);
	}

	public void debitar(Dinheiro quantia) {
		Validate.notNull(quantia, "quantia");
		if (!valor.maiorOuIgualA(quantia)) {
			throw new IllegalStateException("saldo insuficiente");
		}
		this.valor = this.valor.subtrair(quantia);
	}

	public UsuarioId getUsuario() {
		return usuario;
	}

	public Dinheiro getValor() {
		return valor;
	}
}
