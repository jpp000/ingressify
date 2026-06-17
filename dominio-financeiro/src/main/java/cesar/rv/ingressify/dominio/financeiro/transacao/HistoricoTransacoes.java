package cesar.rv.ingressify.dominio.financeiro.transacao;

import java.util.List;

import cesar.rv.ingressify.dominio.financeiro.padroes.iterador.ColecaoTransacoes;
import cesar.rv.ingressify.dominio.financeiro.padroes.iterador.TransacaoIterador;

/**
 * @deprecated Use {@link ColecaoTransacoes} diretamente para suporte a paginação.
 */
@Deprecated
public class HistoricoTransacoes {

	private final ColecaoTransacoes colecao;

	public HistoricoTransacoes(List<Transacao> transacoes) {
		this.colecao = new ColecaoTransacoes(transacoes);
	}

	public TransacaoIterador iterador() {
		return colecao.criarIterador(1, Integer.MAX_VALUE);
	}

	public int total() {
		return colecao.total();
	}
}
