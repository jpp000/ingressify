package cesar.rv.ingressify.aplicacao.marketplace.padroes.template;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.pagamento.Pagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoServico;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaServico;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;

public class RevendaProcessamento extends ProcessamentoPagamentoTemplate {

	private static final BigDecimal TAXA_PLATAFORMA = new BigDecimal("0.10");

	private final AnuncioRevendaServico anuncioRevendaServico;
	private final IngressoServico ingressoServico;
	private final PagamentoServico pagamentoServico;
	private final SaldoServico saldoServico;
	private final TransacaoServico transacaoServico;
	private final AnuncioRevendaId anuncioId;
	private final UsuarioId vendedorId;

	public RevendaProcessamento(AnuncioRevendaServico anuncioRevendaServico, IngressoServico ingressoServico,
			PagamentoServico pagamentoServico, SaldoServico saldoServico, TransacaoServico transacaoServico,
			AnuncioRevendaId anuncioId, UsuarioId vendedorId) {
		Validate.notNull(anuncioRevendaServico, "anuncioRevendaServico");
		Validate.notNull(ingressoServico, "ingressoServico");
		Validate.notNull(pagamentoServico, "pagamentoServico");
		Validate.notNull(saldoServico, "saldoServico");
		Validate.notNull(transacaoServico, "transacaoServico");
		Validate.notNull(anuncioId, "anuncioId");
		Validate.notNull(vendedorId, "vendedorId");
		this.anuncioRevendaServico = anuncioRevendaServico;
		this.ingressoServico = ingressoServico;
		this.pagamentoServico = pagamentoServico;
		this.saldoServico = saldoServico;
		this.transacaoServico = transacaoServico;
		this.anuncioId = anuncioId;
		this.vendedorId = vendedorId;
	}

	@Override
	protected void validar(ContextoProcessamento ctx) {
		AnuncioRevenda anuncio = anuncioRevendaServico.obter(anuncioId);
		if (anuncio.getVendedor().equals(ctx.getCompradorId())) {
			throw new IllegalStateException("vendedor não pode comprar o próprio anúncio");
		}
	}

	@Override
	protected void debitar(ContextoProcessamento ctx) {
		var pagamento = pagamentoServico.criarPendente(
				ctx.getValorTotal(), ctx.getCompradorId(), vendedorId,
				TipoOperacao.REVENDA, ctx.getCorrelacao());
		ctx.setPagamento(pagamento);
		saldoServico.debitar(ctx.getCompradorId(), ctx.getValorTotal());
	}

	@Override
	protected void processarPagamento(ContextoProcessamento ctx) {
		pagamentoServico.confirmar(ctx.getPagamento().getId());
	}

	@Override
	protected void emitirAtivos(ContextoProcessamento ctx) {
		Pagamento pag = ctx.getPagamento();
		Pagamento.Divisao divisao = pag.dividir(TAXA_PLATAFORMA);
		saldoServico.creditar(vendedorId, divisao.vendedor());
		AnuncioRevenda anuncio = anuncioRevendaServico.obter(anuncioId);
		anuncio.getIngressoIds().forEach(iid -> {
			ingressoServico.concluirRevenda(iid, ctx.getCompradorId());
			ctx.adicionarIngresso(iid);
		});
		anuncioRevendaServico.concluir(anuncioId);
	}

	@Override
	protected void registrarTransacao(ContextoProcessamento ctx) {
		LocalDateTime agora = LocalDateTime.now();
		Pagamento pag = ctx.getPagamento();
		Pagamento.Divisao divisao = pag.dividir(TAXA_PLATAFORMA);
		Dinheiro taxaPlataforma = ctx.getValorTotal().subtrair(divisao.vendedor());
		transacaoServico.registrar(new Transacao(ctx.getCompradorId(), TipoTransacao.COMPRA,
				ctx.getValorTotal(), agora, ctx.getCorrelacao()));
		transacaoServico.registrar(new Transacao(vendedorId, TipoTransacao.VENDA,
				divisao.vendedor(), agora, ctx.getCorrelacao()));
		transacaoServico.registrar(new Transacao(vendedorId, TipoTransacao.AJUSTE_SALDO,
				taxaPlataforma, agora, ctx.getCorrelacao()));
	}
}
