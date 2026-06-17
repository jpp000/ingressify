package cesar.rv.ingressify.aplicacao.marketplace.padroes.observador;

import cesar.rv.ingressify.dominio.padroes.observador.PublicadorBase;

public class PublicadorEvento extends PublicadorBase<ContextoCancelamento> {

	public void notificarCancelamento(ContextoCancelamento contexto) {
		notificarTodos(contexto);
	}
}
