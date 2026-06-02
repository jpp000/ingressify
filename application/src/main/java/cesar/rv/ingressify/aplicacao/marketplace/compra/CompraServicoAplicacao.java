package cesar.rv.ingressify.aplicacao.marketplace.compra;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.aplicacao.marketplace.padroes.template.CompraDirectaProcessamento;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.template.ContextoProcessamento;
import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoServico;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.compra.Pedido;
import cesar.rv.ingressify.dominio.marketplace.compra.PedidoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;

import java.time.LocalDateTime;

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
		Dinheiro valorTotal = tipo.getPreco().multiplicar(quantidade);
		UUID correlacao = UUID.randomUUID();

		ContextoProcessamento ctx = new ContextoProcessamento(
				compradorId, tipoIngressoId, quantidade, valorTotal, correlacao);

		CompraDirectaProcessamento processamento = new CompraDirectaProcessamento(
				tipoIngressoServico, tipoIngressoRepositorio, ingressoServico,
				pagamentoServico, saldoServico, transacaoServico);

		List<IngressoId> ids = processamento.executar(ctx);

		pedidoRepositorio.salvar(new Pedido(correlacao, tipoIngressoId, tipo.getEventoId(),
				quantidade, compradorId, valorTotal, LocalDateTime.now()));

		return ids;
	}
}
