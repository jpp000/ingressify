package cesar.rv.ingressify.aplicacao.financeiro.pagamento;

import java.util.List;

import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoId;

public interface PagamentoRepositorioAplicacao {

	List<PagamentoResumo> pesquisarResumos();

	PagamentoResumo obterResumo(PagamentoId id);
}
