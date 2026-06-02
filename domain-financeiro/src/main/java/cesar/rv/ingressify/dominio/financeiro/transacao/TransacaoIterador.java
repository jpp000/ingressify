package cesar.rv.ingressify.dominio.financeiro.transacao;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.Validate;

/**
 * @deprecated Use {@link cesar.rv.ingressify.dominio.financeiro.padroes.iterador.ColecaoTransacoes}
 */
@Deprecated
public class TransacaoIterador
		implements cesar.rv.ingressify.dominio.financeiro.padroes.iterador.TransacaoIterador {

	private final List<Transacao> transacoes;
	private int posicao = 0;
	private Transacao atualTransacao = null;

	public TransacaoIterador(List<Transacao> transacoes) {
		Validate.notNull(transacoes, "transacoes");
		this.transacoes = List.copyOf(transacoes);
	}

	@Override
	public boolean temProximo() {
		return posicao < transacoes.size();
	}

	@Override
	public Transacao proximo() {
		if (!temProximo()) {
			throw new NoSuchElementException("Não há mais transações");
		}
		atualTransacao = transacoes.get(posicao++);
		return atualTransacao;
	}

	@Override
	public Transacao atual() {
		if (atualTransacao == null) {
			throw new IllegalStateException("Chame proximo() antes de atual()");
		}
		return atualTransacao;
	}

	@Override
	public int totalElementos() {
		return transacoes.size();
	}
}
