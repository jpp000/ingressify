package cesar.rv.ingressify.aplicacao.marketplace.reembolso;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.compra.Pedido;
import cesar.rv.ingressify.dominio.marketplace.compra.PedidoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;
import cesar.rv.ingressify.dominio.marketplace.reembolso.MotivoReembolso;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolso;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoId;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoServico;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;

public class ReembolsoServicoAplicacao {

	private final SolicitacaoReembolsoServico solicitacaoReembolsoServico;
	private final SolicitacaoReembolsoRepositorio solicitacaoReembolsoRepositorio;
	private final IngressoServico ingressoServico;
	private final TipoIngressoServico tipoIngressoServico;
	private final TipoIngressoRepositorio tipoIngressoRepositorio;
	private final PedidoRepositorio pedidoRepositorio;
	private final EventoRepositorio eventoRepositorio;
	private final SaldoServico saldoServico;
	private final TransacaoServico transacaoServico;

	public ReembolsoServicoAplicacao(SolicitacaoReembolsoServico solicitacaoReembolsoServico,
			SolicitacaoReembolsoRepositorio solicitacaoReembolsoRepositorio, IngressoServico ingressoServico,
			TipoIngressoServico tipoIngressoServico, TipoIngressoRepositorio tipoIngressoRepositorio,
			PedidoRepositorio pedidoRepositorio, EventoRepositorio eventoRepositorio, SaldoServico saldoServico,
			TransacaoServico transacaoServico) {
		Validate.notNull(solicitacaoReembolsoServico, "solicitacaoReembolsoServico");
		Validate.notNull(solicitacaoReembolsoRepositorio, "solicitacaoReembolsoRepositorio");
		Validate.notNull(ingressoServico, "ingressoServico");
		Validate.notNull(tipoIngressoServico, "tipoIngressoServico");
		Validate.notNull(tipoIngressoRepositorio, "tipoIngressoRepositorio");
		Validate.notNull(pedidoRepositorio, "pedidoRepositorio");
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		Validate.notNull(saldoServico, "saldoServico");
		Validate.notNull(transacaoServico, "transacaoServico");
		this.solicitacaoReembolsoServico = solicitacaoReembolsoServico;
		this.solicitacaoReembolsoRepositorio = solicitacaoReembolsoRepositorio;
		this.ingressoServico = ingressoServico;
		this.tipoIngressoServico = tipoIngressoServico;
		this.tipoIngressoRepositorio = tipoIngressoRepositorio;
		this.pedidoRepositorio = pedidoRepositorio;
		this.eventoRepositorio = eventoRepositorio;
		this.saldoServico = saldoServico;
		this.transacaoServico = transacaoServico;
	}

	public SolicitacaoReembolsoId solicitar(IngressoId ingressoId, UsuarioId solicitanteId) {
		Ingresso ingresso = ingressoServico.obter(ingressoId);
		if (!ingresso.getProprietario().equals(solicitanteId)) {
			throw new IllegalStateException("ingresso não pertence ao solicitante");
		}
		StatusIngresso st = ingresso.getStatus();
		if (st == StatusIngresso.UTILIZADO || st == StatusIngresso.REEMBOLSADO || st == StatusIngresso.CANCELADO) {
			throw new IllegalStateException("ingresso não elegível para reembolso");
		}
		if (solicitacaoReembolsoRepositorio.pesquisarAtivaPorIngresso(ingressoId).isPresent()) {
			throw new IllegalStateException("já existe solicitação ativa para o ingresso");
		}
		TipoIngresso tipo = tipoIngressoRepositorio.obter(ingresso.getTipoIngressoId());
		Evento evento = eventoRepositorio.obter(ingresso.getEventoId());
		LocalDateTime agora = LocalDateTime.now();
		Pedido pedidoMaisRecente = pedidoRepositorio
				.pesquisarPorCompradorETipoIngressoOrdenadoDesc(solicitanteId, tipo.getId())
				.stream()
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("pedido não encontrado para o ingresso"));
		if (pedidoMaisRecente.getCriadaEm().isBefore(agora.minusDays(7))) {
			throw new IllegalStateException("fora do prazo de reembolso");
		}
		if (!evento.getDataHora().isAfter(agora.plusHours(48))) {
			throw new IllegalStateException("fora do prazo de reembolso");
		}
		ingresso.bloquearParaReembolso();
		ingressoServico.salvar(ingresso);
		SolicitacaoReembolso solicitacao = new SolicitacaoReembolso(ingressoId, solicitanteId, MotivoReembolso.VOLUNTARIO,
				tipo.getPreco(), agora);
		solicitacaoReembolsoServico.salvar(solicitacao);
		_aprovar(solicitacao.getId(), ingresso, tipo, solicitanteId, agora);
		return solicitacao.getId();
	}

	public void aprovarPorCancelamento(IngressoId ingressoId, UsuarioId solicitanteId, Dinheiro valor) {
		LocalDateTime agora = LocalDateTime.now();
		Ingresso ingresso = ingressoServico.obter(ingressoId);
		TipoIngresso tipo = tipoIngressoRepositorio.obter(ingresso.getTipoIngressoId());
		SolicitacaoReembolso solicitacao = new SolicitacaoReembolso(ingressoId, solicitanteId,
				MotivoReembolso.EVENTO_CANCELADO, valor, agora);
		solicitacaoReembolsoServico.salvar(solicitacao);
		_aprovar(solicitacao.getId(), ingresso, tipo, solicitanteId, agora);
	}

	private void _aprovar(SolicitacaoReembolsoId solicitacaoId, Ingresso ingresso, TipoIngresso tipo,
			UsuarioId solicitanteId, LocalDateTime agora) {
		solicitacaoReembolsoServico.aprovar(solicitacaoId, agora);
		SolicitacaoReembolso s = solicitacaoReembolsoServico.obter(solicitacaoId);
		saldoServico.creditar(solicitanteId, s.getValor());
		transacaoServico.registrar(new Transacao(solicitanteId, TipoTransacao.REEMBOLSO, s.getValor(), agora,
				UUID.randomUUID()));
		Ingresso ingAtual = ingressoServico.obter(ingresso.getId());
		ingAtual.marcarReembolsado();
		ingressoServico.salvar(ingAtual);
		tipoIngressoServico.devolver(tipo.getId(), 1);
	}
}
