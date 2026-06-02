package cesar.rv.ingressify.dominio.financeiro.padroes.iterador;

import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.padroes.iterador.IIterador;

public interface TransacaoIterador extends IIterador<Transacao> {

	Transacao atual();

	int totalElementos();
}
