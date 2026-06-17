package cesar.rv.ingressify.aplicacao.marketplace.padroes.template;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoServico;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;

public class CompraDirectaProcessamento extends ProcessamentoPagamentoTemplate {

	private final TipoIngressoServico tipoIngressoServico;
	private final TipoIngressoRepositorio tipoIngressoRepositorio;
	private final IngressoServico ingressoServico;
	private final PagamentoServico pagamentoServico;
	private final SaldoServico saldoServico;
	private final TransacaoServico transacaoServico;

	public CompraDirectaProcessamento(TipoIngressoServico tipoIngressoServico,
			TipoIngressoRepositorio tipoIngressoRepositorio, IngressoServico ingressoServico,
			PagamentoServico pagamentoServico, SaldoServico saldoServico, TransacaoServico transacaoServico) {
		Validate.notNull(tipoIngressoServico, "tipoIngressoServico");
		Validate.notNull(tipoIngressoRepositorio, "tipoIngressoRepositorio");
		Validate.notNull(ingressoServico, "ingressoServico");
		Validate.notNull(pagamentoServico, "pagamentoServico");
		Validate.notNull(saldoServico, "saldoServico");
		Validate.notNull(transacaoServico, "transacaoServico");
		this.tipoIngressoServico = tipoIngressoServico;
		this.tipoIngressoRepositorio = tipoIngressoRepositorio;
		this.ingressoServico = ingressoServico;
		this.pagamentoServico = pagamentoServico;
		this.saldoServico = saldoServico;
		this.transacaoServico = transacaoServico;
	}

	@Override
	protected void validar(ContextoProcessamento ctx) {
		TipoIngresso tipo = tipoIngressoRepositorio.obter(ctx.getTipoIngressoId());
		if (ctx.getQuantidade() > tipo.getQuantidadeDisponivel()) {
			throw new IllegalStateException("quantidade indisponível: apenas "
					+ tipo.getQuantidadeDisponivel() + " disponíveis");
		}
		if (ctx.isMeiaEntrada()) {
			tipo.reservarMeia(ctx.getQuantidade());
		} else {
			tipo.reservar(ctx.getQuantidade());
		}
		tipoIngressoServico.salvar(tipo);
	}

	@Override
	protected void debitar(ContextoProcessamento ctx) {
		var pagamento = pagamentoServico.criarPendente(
				ctx.getValorTotal(), ctx.getCompradorId(), null,
				TipoOperacao.COMPRA_DIRETA, ctx.getCorrelacao());
		ctx.setPagamento(pagamento);
		try {
			saldoServico.debitar(ctx.getCompradorId(), ctx.getValorTotal());
		} catch (RuntimeException e) {
			TipoIngresso tipo = tipoIngressoRepositorio.obter(ctx.getTipoIngressoId());
			if (ctx.isMeiaEntrada()) {
				tipo.devolverMeia(ctx.getQuantidade());
			} else {
				tipo.devolver(ctx.getQuantidade());
			}
			tipoIngressoServico.salvar(tipo);
			pagamentoServico.rejeitar(pagamento.getId());
			throw e;
		}
	}

	@Override
	protected void processarPagamento(ContextoProcessamento ctx) {
		pagamentoServico.confirmar(ctx.getPagamento().getId());
	}

	@Override
	protected void emitirAtivos(ContextoProcessamento ctx) {
		TipoIngresso tipo = tipoIngressoRepositorio.obter(ctx.getTipoIngressoId());
		for (int n = 0; n < ctx.getQuantidade(); n++) {
			Ingresso ingresso = new Ingresso(ctx.getTipoIngressoId(), tipo.getEventoId(), ctx.getCompradorId());
			if (ctx.isMeiaEntrada()) {
				ingresso.definirMeiaEntrada(ctx.getDocumento());
			}
			ingressoServico.salvar(ingresso);
			ctx.adicionarIngresso(ingresso.getId());
		}
	}

	@Override
	protected void registrarTransacao(ContextoProcessamento ctx) {
		transacaoServico.registrar(new Transacao(
				ctx.getCompradorId(), TipoTransacao.COMPRA,
				ctx.getValorTotal(), LocalDateTime.now(),
				ctx.getCorrelacao()));
	}
}
