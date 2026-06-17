package cesar.rv.ingressify.aplicacao.marketplace.reembolso;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.Papel;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioServico;
import cesar.rv.ingressify.dominio.marketplace.compra.Pedido;
import cesar.rv.ingressify.dominio.marketplace.compra.PedidoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.estrategia.ReembolsoCancelamentoEstrategia;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.estrategia.ReembolsoVoluntarioEstrategia;
import cesar.rv.ingressify.dominio.padroes.estrategia.EstrategiaReembolso;
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
	private final UsuarioServico usuarioServico;

	public ReembolsoServicoAplicacao(SolicitacaoReembolsoServico solicitacaoReembolsoServico,
			SolicitacaoReembolsoRepositorio solicitacaoReembolsoRepositorio, IngressoServico ingressoServico,
			TipoIngressoServico tipoIngressoServico, TipoIngressoRepositorio tipoIngressoRepositorio,
			PedidoRepositorio pedidoRepositorio, EventoRepositorio eventoRepositorio, SaldoServico saldoServico,
			TransacaoServico transacaoServico, UsuarioServico usuarioServico) {
		Validate.notNull(solicitacaoReembolsoServico, "solicitacaoReembolsoServico");
		Validate.notNull(solicitacaoReembolsoRepositorio, "solicitacaoReembolsoRepositorio");
		Validate.notNull(ingressoServico, "ingressoServico");
		Validate.notNull(tipoIngressoServico, "tipoIngressoServico");
		Validate.notNull(tipoIngressoRepositorio, "tipoIngressoRepositorio");
		Validate.notNull(pedidoRepositorio, "pedidoRepositorio");
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		Validate.notNull(saldoServico, "saldoServico");
		Validate.notNull(transacaoServico, "transacaoServico");
		Validate.notNull(usuarioServico, "usuarioServico");
		this.solicitacaoReembolsoServico = solicitacaoReembolsoServico;
		this.solicitacaoReembolsoRepositorio = solicitacaoReembolsoRepositorio;
		this.ingressoServico = ingressoServico;
		this.tipoIngressoServico = tipoIngressoServico;
		this.tipoIngressoRepositorio = tipoIngressoRepositorio;
		this.pedidoRepositorio = pedidoRepositorio;
		this.eventoRepositorio = eventoRepositorio;
		this.saldoServico = saldoServico;
		this.transacaoServico = transacaoServico;
		this.usuarioServico = usuarioServico;
	}

	private EstrategiaReembolso selecionarEstrategia(MotivoReembolso motivo, int prazoReembolsoDias) {
		return switch (motivo) {
			case VOLUNTARIO -> new ReembolsoVoluntarioEstrategia(prazoReembolsoDias);
			case EVENTO_CANCELADO -> new ReembolsoCancelamentoEstrategia();
		};
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
		LocalDateTime dataCompra = pedidoRepositorio
				.pesquisarPorCompradorETipoIngressoOrdenadoDesc(solicitanteId, tipo.getId())
				.stream()
				.findFirst()
				.map(Pedido::getCriadaEm)
				.orElse(agora);
		selecionarEstrategia(MotivoReembolso.VOLUNTARIO, evento.getPrazoReembolsoDias())
				.validar(dataCompra, evento.getDataHora());
		ingresso.bloquearParaReembolso();
		ingressoServico.salvar(ingresso);
		SolicitacaoReembolso solicitacao = new SolicitacaoReembolso(ingressoId, solicitanteId,
				MotivoReembolso.VOLUNTARIO, tipo.getPreco(), agora);
		solicitacaoReembolsoServico.salvar(solicitacao);
		return solicitacao.getId();
	}

	public void iniciarAnalise(SolicitacaoReembolsoId id, UsuarioId adminId) {
		exigirAdmin(adminId);
		solicitacaoReembolsoServico.iniciarAnalise(id, LocalDateTime.now());
	}

	public void aprovarManual(SolicitacaoReembolsoId id, UsuarioId adminId) {
		exigirAdmin(adminId);
		SolicitacaoReembolso s = solicitacaoReembolsoServico.obter(id);
		Ingresso ingresso = ingressoServico.obter(s.getIngressoId());
		TipoIngresso tipo = tipoIngressoRepositorio.obter(ingresso.getTipoIngressoId());
		_aprovar(id, ingresso, tipo, s.getSolicitanteId(), LocalDateTime.now());
	}

	public void recusarManual(SolicitacaoReembolsoId id, UsuarioId adminId) {
		exigirAdmin(adminId);
		SolicitacaoReembolso s = solicitacaoReembolsoServico.obter(id);
		Ingresso ingresso = ingressoServico.obter(s.getIngressoId());
		ingresso.desbloquearReembolso();
		ingressoServico.salvar(ingresso);
		solicitacaoReembolsoServico.recusar(id, LocalDateTime.now());
	}

	public void cancelarPeloUsuario(SolicitacaoReembolsoId id, UsuarioId usuarioId) {
		SolicitacaoReembolso s = solicitacaoReembolsoServico.obter(id);
		if (!s.getSolicitanteId().equals(usuarioId)) {
			throw new IllegalStateException("solicitação não pertence ao usuário");
		}
		Ingresso ingresso = ingressoServico.obter(s.getIngressoId());
		ingresso.desbloquearReembolso();
		ingressoServico.salvar(ingresso);
		solicitacaoReembolsoServico.cancelar(id, LocalDateTime.now());
	}

	public List<SolicitacaoReembolso> listar(UsuarioId adminId) {
		exigirAdmin(adminId);
		return solicitacaoReembolsoRepositorio.listarTodas();
	}

	public List<SolicitacaoReembolso> listarPorUsuario(UsuarioId usuarioId) {
		return solicitacaoReembolsoRepositorio.pesquisarPorSolicitante(usuarioId);
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

	private void exigirAdmin(UsuarioId usuarioId) {
		if (!usuarioServico.obter(usuarioId).temPapel(Papel.ADMIN)) {
			throw new IllegalStateException("acesso restrito a administradores");
		}
	}
}
