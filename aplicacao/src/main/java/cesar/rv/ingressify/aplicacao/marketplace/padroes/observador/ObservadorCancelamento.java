package cesar.rv.ingressify.aplicacao.marketplace.padroes.observador;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.aplicacao.marketplace.reembolso.ReembolsoServicoAplicacao;
import cesar.rv.ingressify.dominio.padroes.observador.Observador;

public class ObservadorCancelamento implements Observador<ContextoCancelamento> {

	private final ReembolsoServicoAplicacao reembolsoServico;

	public ObservadorCancelamento(ReembolsoServicoAplicacao reembolsoServico) {
		Validate.notNull(reembolsoServico, "reembolsoServico");
		this.reembolsoServico = reembolsoServico;
	}

	@Override
	public void notificar(ContextoCancelamento ctx) {
		for (IngressoCancelado ic : ctx.ingressosCancelados()) {
			reembolsoServico.aprovarPorCancelamento(ic.ingressoId(), ic.proprietario(), ic.valorReembolso());
		}
	}
}
