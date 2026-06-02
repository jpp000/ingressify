package cesar.rv.ingressify.dominio.financeiro.padroes.iterador;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;

public class ColecaoTransacoes {

	private final List<Transacao> transacoesOrdenadas;

	public ColecaoTransacoes(List<Transacao> transacoes) {
		Validate.notNull(transacoes, "transacoes");
		this.transacoesOrdenadas = transacoes.stream()
				.sorted(Comparator.comparing(Transacao::getData).reversed())
				.collect(Collectors.toUnmodifiableList());
	}

	public TransacaoIterador criarIterador(int numeroPagina, int limite) {
		Validate.isTrue(numeroPagina >= 1, "numeroPagina deve ser >= 1");
		Validate.isTrue(limite >= 1, "limite deve ser >= 1");
		int inicio = (numeroPagina - 1) * limite;
		if (inicio >= transacoesOrdenadas.size()) {
			return new TransacaoIteradorImpl(List.of());
		}
		int fim = Math.min(inicio + limite, transacoesOrdenadas.size());
		return new TransacaoIteradorImpl(transacoesOrdenadas.subList(inicio, fim));
	}

	public int total() {
		return transacoesOrdenadas.size();
	}
}
