package cesar.rv.ingressify.aplicacao.marketplace.compra;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.aplicacao.marketplace.padroes.template.CompraDirectaProcessamento;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.template.ContextoProcessamento;
import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoServico;
import cesar.rv.ingressify.dominio.financeiro.saldo.Saldo;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.compra.Pedido;
import cesar.rv.ingressify.dominio.marketplace.compra.PedidoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.cupom.CupomServico;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
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
	private final CupomServico cupomServico;

	public CompraServicoAplicacao(TipoIngressoServico tipoIngressoServico,
			TipoIngressoRepositorio tipoIngressoRepositorio, IngressoServico ingressoServico,
			PagamentoServico pagamentoServico, SaldoServico saldoServico, TransacaoServico transacaoServico,
			PedidoRepositorio pedidoRepositorio, CupomServico cupomServico) {
		Validate.notNull(tipoIngressoServico, "tipoIngressoServico");
		Validate.notNull(tipoIngressoRepositorio, "tipoIngressoRepositorio");
		Validate.notNull(ingressoServico, "ingressoServico");
		Validate.notNull(pagamentoServico, "pagamentoServico");
		Validate.notNull(saldoServico, "saldoServico");
		Validate.notNull(transacaoServico, "transacaoServico");
		Validate.notNull(pedidoRepositorio, "pedidoRepositorio");
		Validate.notNull(cupomServico, "cupomServico");
		this.tipoIngressoServico = tipoIngressoServico;
		this.tipoIngressoRepositorio = tipoIngressoRepositorio;
		this.ingressoServico = ingressoServico;
		this.pagamentoServico = pagamentoServico;
		this.saldoServico = saldoServico;
		this.transacaoServico = transacaoServico;
		this.pedidoRepositorio = pedidoRepositorio;
		this.cupomServico = cupomServico;
	}

	public List<IngressoId> comprar(UsuarioId compradorId, TipoIngressoId tipoIngressoId, int quantidade) {
		return comprar(compradorId, tipoIngressoId, quantidade, false, null, null);
	}

	public List<IngressoId> comprar(UsuarioId compradorId, TipoIngressoId tipoIngressoId, int quantidade,
			boolean meiaEntrada, String documento, String codigoCupom) {
		return comprarPedido(compradorId,
				List.of(new ItemPedidoCompra(tipoIngressoId.getId(), quantidade, meiaEntrada, documento)),
				codigoCupom);
	}

	/**
	 * Processa um pedido com um ou mais itens. O cupom (se houver) é validado e consumido
	 * uma única vez sobre o total bruto, e o desconto é distribuído proporcionalmente entre
	 * os itens. Todos os itens devem pertencer ao mesmo evento.
	 */
	public List<IngressoId> comprarPedido(UsuarioId compradorId, List<ItemPedidoCompra> itens, String codigoCupom) {
		Validate.notEmpty(itens, "itens");

		EventoId eventoId = null;
		Dinheiro totalBruto = Dinheiro.ZERO;
		List<Dinheiro> brutoPorItem = new ArrayList<>();

		for (ItemPedidoCompra item : itens) {
			Validate.isTrue(item.quantidade() > 0, "quantidade deve ser > 0");
			TipoIngresso tipo = tipoIngressoRepositorio.obter(new TipoIngressoId(item.tipoIngressoId()));
			if (eventoId == null) {
				eventoId = tipo.getEventoId();
			} else if (!eventoId.equals(tipo.getEventoId())) {
				throw new IllegalStateException("todos os itens do pedido devem ser do mesmo evento");
			}
			Dinheiro unitario;
			if (item.meiaEntrada()) {
				Validate.notBlank(item.documento(), "documento é obrigatório para meia-entrada");
				unitario = tipo.precoMeia();
			} else {
				unitario = tipo.getPreco();
			}
			Dinheiro bruto = unitario.multiplicar(item.quantidade());
			brutoPorItem.add(bruto);
			totalBruto = totalBruto.somar(bruto);
		}

		Dinheiro totalFinal = totalBruto;
		if (codigoCupom != null && !codigoCupom.isBlank()) {
			totalFinal = cupomServico.consumir(codigoCupom, totalBruto, eventoId, LocalDateTime.now());
		}

		Saldo saldo = saldoServico.obter(compradorId);
		if (!saldo.getValor().maiorOuIgualA(totalFinal)) {
			throw new IllegalStateException("saldo insuficiente");
		}

		BigDecimal fator = totalBruto.getValor().signum() == 0
				? BigDecimal.ONE
				: totalFinal.getValor().divide(totalBruto.getValor(), 10, RoundingMode.HALF_UP);

		List<IngressoId> criados = new ArrayList<>();
		for (int i = 0; i < itens.size(); i++) {
			ItemPedidoCompra item = itens.get(i);
			TipoIngressoId tipoId = new TipoIngressoId(item.tipoIngressoId());
			Dinheiro valorItem = new Dinheiro(brutoPorItem.get(i).getValor().multiply(fator));
			UUID correlacao = UUID.randomUUID();

			ContextoProcessamento ctx = new ContextoProcessamento(
					compradorId, tipoId, item.quantidade(), valorItem, correlacao,
					item.meiaEntrada(), item.documento());

			CompraDirectaProcessamento processamento = new CompraDirectaProcessamento(
					tipoIngressoServico, tipoIngressoRepositorio, ingressoServico,
					pagamentoServico, saldoServico, transacaoServico);

			criados.addAll(processamento.executar(ctx));

			pedidoRepositorio.salvar(new Pedido(correlacao, tipoId, eventoId,
					item.quantidade(), compradorId, valorItem, LocalDateTime.now()));
		}
		return criados;
	}
}
