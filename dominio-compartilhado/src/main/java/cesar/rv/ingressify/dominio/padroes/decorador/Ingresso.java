package cesar.rv.ingressify.dominio.padroes.decorador;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;

public interface Ingresso {

	Dinheiro obterPreco();

	String obterDescricao();
}
