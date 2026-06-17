package cesar.rv.ingressify.dominio.padroes.decorador;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;

public abstract class IngressoDecorador implements Ingresso {

	protected final Ingresso decorado;

	protected IngressoDecorador(Ingresso decorado) {
		Validate.notNull(decorado, "decorado");
		this.decorado = decorado;
	}

	@Override
	public Dinheiro obterPreco() {
		return decorado.obterPreco();
	}

	@Override
	public String obterDescricao() {
		return decorado.obterDescricao();
	}
}
