package cesar.rv.ingressify.dominio.financeiro.padroes.iterador;

import java.util.List;
import java.util.NoSuchElementException;

import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;

class TransacaoIteradorImpl implements TransacaoIterador {

	private final List<Transacao> pagina;
	private int posicao = 0;
	private Transacao atualTransacao = null;

	TransacaoIteradorImpl(List<Transacao> pagina) {
		this.pagina = List.copyOf(pagina);
	}

	@Override
	public boolean temProximo() {
		return posicao < pagina.size();
	}

	@Override
	public Transacao proximo() {
		if (!temProximo()) {
			throw new NoSuchElementException("Não há mais transações na página");
		}
		atualTransacao = pagina.get(posicao++);
		return atualTransacao;
	}

	@Override
	public Transacao atual() {
		if (atualTransacao == null) {
			throw new IllegalStateException("Nenhuma transação consumida ainda; chame proximo() primeiro");
		}
		return atualTransacao;
	}

	@Override
	public int totalElementos() {
		return pagina.size();
	}
}
