package cesar.rv.ingressify.aplicacao.pagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cesar.rv.ingressify.dominio.financeiro.pagamento.Pagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoId;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoServico;
import cesar.rv.ingressify.dominio.financeiro.pagamento.StatusPagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaServico;
import cesar.rv.ingressify.dominio.marketplace.compra.CompraPendente;
import cesar.rv.ingressify.dominio.marketplace.compra.CompraPendenteRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;

@Service
public class ConfirmarPagamentoServicoAplicacao {

	private static final BigDecimal PERCENTUAL_PLATAFORMA = new BigDecimal("0.10");

	private final PagamentoServico pagamentoServico;
	private final CompraPendenteRepositorio compraPendenteRepositorio;
	private final IngressoServico ingressoServico;
	private final TipoIngressoServico tipoIngressoServico;
	private final AnuncioRevendaServico anuncioRevendaServico;
	private final SaldoServico saldoServico;
	private final TransacaoRepositorio transacaoRepositorio;

	public ConfirmarPagamentoServicoAplicacao(PagamentoServico pagamentoServico,
			CompraPendenteRepositorio compraPendenteRepositorio, IngressoServico ingressoServico,
			TipoIngressoServico tipoIngressoServico, AnuncioRevendaServico anuncioRevendaServico,
			SaldoServico saldoServico, TransacaoRepositorio transacaoRepositorio) {
		Validate.notNull(pagamentoServico, "pagamentoServico");
		Validate.notNull(compraPendenteRepositorio, "compraPendenteRepositorio");
		Validate.notNull(ingressoServico, "ingressoServico");
		Validate.notNull(tipoIngressoServico, "tipoIngressoServico");
		Validate.notNull(anuncioRevendaServico, "anuncioRevendaServico");
		Validate.notNull(saldoServico, "saldoServico");
		Validate.notNull(transacaoRepositorio, "transacaoRepositorio");
		this.pagamentoServico = pagamentoServico;
		this.compraPendenteRepositorio = compraPendenteRepositorio;
		this.ingressoServico = ingressoServico;
		this.tipoIngressoServico = tipoIngressoServico;
		this.anuncioRevendaServico = anuncioRevendaServico;
		this.saldoServico = saldoServico;
		this.transacaoRepositorio = transacaoRepositorio;
	}

	@Transactional
	public void confirmar(PagamentoId id) {
		Pagamento pag = pagamentoServico.confirmar(id);
		UUID ref = pag.getCorrelacaoId();
		if (pag.getTipo() == TipoOperacao.COMPRA_DIRETA) {
			CompraPendente compra = compraPendenteRepositorio.buscar(pag.getCorrelacaoId())
					.orElseThrow(() -> new IllegalStateException("compra pendente não encontrada"));
			ingressoServico.emitir(compra, compra.getComprador());
			compraPendenteRepositorio.remover(compra.getId());
			saldoServico.debitar(pag.getComprador(), pag.getValor());
			transacaoRepositorio.salvar(new Transacao(pag.getComprador(), TipoTransacao.COMPRA, pag.getValor(),
					LocalDateTime.now(), ref));
			return;
		}
		if (pag.getTipo() == TipoOperacao.REVENDA) {
			AnuncioRevenda anuncio = anuncioRevendaServico.obterPorCorrelacao(pag.getCorrelacaoId());
			AnuncioRevendaId anuncioId = anuncio.getId();
			Validate.notNull(anuncioId, "anuncio.id");
			anuncioRevendaServico.concluir(anuncioId);
			ingressoServico.concluirRevenda(anuncio.getIngressoId(), pag.getComprador());
			Pagamento.Divisao divisao = pag.dividir(PERCENTUAL_PLATAFORMA);
			Validate.notNull(pag.getVendedor(), "vendedor");
			saldoServico.debitar(pag.getComprador(), pag.getValor());
			saldoServico.creditar(pag.getVendedor(), divisao.vendedor());
			transacaoRepositorio.salvar(new Transacao(pag.getComprador(), TipoTransacao.COMPRA, pag.getValor(),
					LocalDateTime.now(), ref));
			transacaoRepositorio.salvar(new Transacao(pag.getVendedor(), TipoTransacao.VENDA, divisao.vendedor(),
					LocalDateTime.now(), ref));
		}
	}

	@Transactional
	public void rejeitar(PagamentoId id) {
		Pagamento p = pagamentoServico.obter(id);
		if (p.getStatus() != StatusPagamento.PENDENTE) {
			throw new IllegalStateException("somente pagamento pendente pode ser rejeitado");
		}
		if (p.getTipo() == TipoOperacao.COMPRA_DIRETA) {
			compraPendenteRepositorio.buscar(p.getCorrelacaoId()).ifPresent(compra -> {
				tipoIngressoServico.devolver(compra.getTipoIngressoId(), compra.getQuantidade());
				compraPendenteRepositorio.remover(compra.getId());
			});
		} else if (p.getTipo() == TipoOperacao.REVENDA) {
			anuncioRevendaServico.cancelarReservaPorCorrelacao(p.getCorrelacaoId());
		}
		pagamentoServico.rejeitar(id);
	}
}
