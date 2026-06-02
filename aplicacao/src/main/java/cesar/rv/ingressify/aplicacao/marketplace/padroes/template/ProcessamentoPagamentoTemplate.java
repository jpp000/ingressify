package cesar.rv.ingressify.aplicacao.marketplace.padroes.template;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

public abstract class ProcessamentoPagamentoTemplate {

	public final List<IngressoId> executar(ContextoProcessamento ctx) {
		validar(ctx);
		debitar(ctx);
		processarPagamento(ctx);
		emitirAtivos(ctx);
		registrarTransacao(ctx);
		return ctx.getIngressosCriados();
	}

	protected abstract void validar(ContextoProcessamento ctx);

	protected abstract void debitar(ContextoProcessamento ctx);

	protected abstract void processarPagamento(ContextoProcessamento ctx);

	protected abstract void emitirAtivos(ContextoProcessamento ctx);

	protected abstract void registrarTransacao(ContextoProcessamento ctx);
}
