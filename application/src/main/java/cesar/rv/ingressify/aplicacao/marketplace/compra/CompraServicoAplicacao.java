package cesar.rv.ingressify.aplicacao.marketplace.compra;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.pagamento.Pagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoId;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoServico;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.compra.Pedido;
import cesar.rv.ingressify.dominio.marketplace.compra.PedidoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;

public class CompraServicoAplicacao {

	private final TipoIngressoServico tipoIngressoServico;
	private final TipoIngressoRepositorio tipoIngressoRepositorio;
	private final IngressoServico ingressoServico;
	private final PagamentoServico pagamentoServico;
	private final SaldoServico saldoServico;
	private final TransacaoServico transacaoServico;
	private final PedidoRepositorio pedidoRepositorio;

	public CompraServicoAplicacao(TipoIngressoServico tipoIngressoServico,
			TipoIngressoRepositorio tipoIngressoRepositorio, IngressoServico ingressoServico,
			PagamentoServico pagamentoServico, SaldoServico saldoServico, TransacaoServico transacaoServico,
			PedidoRepositorio pedidoRepositorio) {
		Validate.notNull(tipoIngressoServico, "tipoIngressoServico");
		Validate.notNull(tipoIngressoRepositorio, "tipoIngressoRepositorio");
		Validate.notNull(ingressoServico, "ingressoServico");
		Validate.notNull(pagamentoServico, "pagamentoServico");
		Validate.notNull(saldoServico, "saldoServico");
		Validate.notNull(transacaoServico, "transacaoServico");
		Validate.notNull(pedidoRepositorio, "pedidoRepositorio");
		this.tipoIngressoServico = tipoIngressoServico;
		this.tipoIngressoRepositorio = tipoIngressoRepositorio;
		this.ingressoServico = ingressoServico;
		this.pagamentoServico = pagamentoServico;
		this.saldoServico = saldoServico;
		this.transacaoServico = transacaoServico;
		this.pedidoRepositorio = pedidoRepositorio;
	}

	public List<IngressoId> comprar(UsuarioId compradorId, TipoIngressoId tipoIngressoId, int quantidade) {
		Validate.isTrue(quantidade > 0, "quantidade deve ser > 0");
		TipoIngresso tipo = tipoIngressoRepositorio.obter(tipoIngressoId);
		if (quantidade > tipo.getQuantidadeDisponivel()) {
			throw new IllegalStateException("quantidade indisponível");
		}
		tipo.reservar(quantidade);
		tipoIngressoServico.salvar(tipo);
		Dinheiro valorTotal = tipo.getPreco().multiplicar(quantidade);
		UUID correlacao = UUID.randomUUID();
		Pagamento pagamento = pagamentoServico.criarPendente(valorTotal, compradorId, null, TipoOperacao.COMPRA_DIRETA,
				correlacao);
		PagamentoId pagamentoId = pagamento.getId();
		try {
			saldoServico.debitar(compradorId, valorTotal);
		} catch (RuntimeException e) {
			tipo.devolver(quantidade);
			tipoIngressoServico.salvar(tipo);
			pagamentoServico.rejeitar(pagamentoId);
			throw e;
		}
		pagamentoServico.confirmar(pagamentoId);
		List<IngressoId> ids = new ArrayList<>();
		for (int n = 0; n < quantidade; n++) {
			Ingresso ingresso = new Ingresso(tipoIngressoId, tipo.getEventoId(), compradorId);
			ingressoServico.salvar(ingresso);
			ids.add(ingresso.getId());
		}
		UUID pedidoId = UUID.randomUUID();
		pedidoRepositorio.salvar(new Pedido(pedidoId, tipoIngressoId, tipo.getEventoId(), quantidade, compradorId,
				valorTotal, LocalDateTime.now()));
		transacaoServico.registrar(new Transacao(compradorId, TipoTransacao.COMPRA, valorTotal, LocalDateTime.now(),
				pedidoId));
		return ids;
	}
}
