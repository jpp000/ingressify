package cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

public interface TipoIngressoResumo {

	TipoIngressoId getId();

	String getNome();

	Dinheiro getPreco();

	int getQuantidadeDisponivel();

	int getQuantidadeTotal();

	String getDescricao();
}
