package cesar.rv.ingressify.aplicacao.financeiro.extrato;

import java.util.List;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;

public interface ExtratoResumo {

	Dinheiro getSaldo();

	List<MovimentacaoResumo> getMovimentacoes();
}
