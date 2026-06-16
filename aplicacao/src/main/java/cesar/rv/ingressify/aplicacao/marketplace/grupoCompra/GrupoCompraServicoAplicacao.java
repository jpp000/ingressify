package cesar.rv.ingressify.aplicacao.marketplace.grupoCompra;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.pagamento.Pagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoServico;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompra;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompraId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompraRepositorio;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompraServico;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupo;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupoEntrada;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.StatusGrupoCompra;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.StatusParticipanteGrupo;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

public class GrupoCompraServicoAplicacao {

	private final GrupoCompraServico grupoCompraServico;
	private final GrupoCompraRepositorio grupoCompraRepositorio;
	private final ParticipanteGrupoRepositorio participanteGrupoRepositorio;
	private final IngressoServico ingressoServico;
	private final PagamentoServico pagamentoServico;
	private final SaldoServico saldoServico;
	private final TransacaoServico transacaoServico;

	public GrupoCompraServicoAplicacao(GrupoCompraServico grupoCompraServico,
			GrupoCompraRepositorio grupoCompraRepositorio, ParticipanteGrupoRepositorio participanteGrupoRepositorio,
			IngressoServico ingressoServico, PagamentoServico pagamentoServico, SaldoServico saldoServico,
			TransacaoServico transacaoServico) {
		Validate.notNull(grupoCompraServico, "grupoCompraServico");
		Validate.notNull(grupoCompraRepositorio, "grupoCompraRepositorio");
		Validate.notNull(participanteGrupoRepositorio, "participanteGrupoRepositorio");
		Validate.notNull(ingressoServico, "ingressoServico");
		Validate.notNull(pagamentoServico, "pagamentoServico");
		Validate.notNull(saldoServico, "saldoServico");
		Validate.notNull(transacaoServico, "transacaoServico");
		this.grupoCompraServico = grupoCompraServico;
		this.grupoCompraRepositorio = grupoCompraRepositorio;
		this.participanteGrupoRepositorio = participanteGrupoRepositorio;
		this.ingressoServico = ingressoServico;
		this.pagamentoServico = pagamentoServico;
		this.saldoServico = saldoServico;
		this.transacaoServico = transacaoServico;
	}

	public GrupoCompra criar(EventoId eventoId, TipoIngressoId tipoIngressoId, UsuarioId liderId,
			List<ParticipanteGrupoEntrada> entradas, LocalDateTime prazoPagamento) {
		return grupoCompraServico.criar(eventoId, tipoIngressoId, liderId, entradas, prazoPagamento);
	}

	public void pagarParticipacao(GrupoCompraId grupoId, UsuarioId usuarioId) {
		GrupoCompra grupo = grupoCompraServico.obter(grupoId);

		if (grupo.prazoExpirado(LocalDateTime.now())) {
			expirarSeNecessario(grupoId);
			throw new IllegalStateException("prazo de pagamento do grupo expirou");
		}
		if (grupo.getStatus() != StatusGrupoCompra.ABERTO) {
			throw new IllegalStateException("grupo de compra não está ABERTO");
		}

		ParticipanteGrupo participante = grupoCompraServico.participantes(grupoId).stream()
				.filter(p -> p.getUsuarioId().equals(usuarioId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("participante não encontrado no grupo"));

		if (participante.getStatus() != StatusParticipanteGrupo.PENDENTE) {
			throw new IllegalStateException("participante já processado");
		}

		// Todas as pré-condições já foram validadas acima; a partir daqui movimentamos
		// dinheiro, então qualquer falha precisa ser compensada (sem @Transactional no projeto).
		UUID correlacao = UUID.randomUUID();
		Pagamento pagamento = pagamentoServico.criarPendente(
				participante.getValor(), usuarioId, null, TipoOperacao.COMPRA_GRUPO, correlacao);
		try {
			saldoServico.debitar(usuarioId, participante.getValor());
		} catch (RuntimeException e) {
			pagamentoServico.rejeitar(pagamento.getId());
			throw e;
		}
		pagamentoServico.confirmar(pagamento.getId());

		grupoCompraServico.confirmarPagamentoParticipante(grupoId, usuarioId);

		transacaoServico.registrar(new Transacao(
				usuarioId, TipoTransacao.COMPRA, participante.getValor(), LocalDateTime.now(), correlacao));

		List<ParticipanteGrupo> participantes = grupoCompraServico.participantes(grupoId);
		boolean todosPagos = participantes.stream().allMatch(p -> p.getStatus() == StatusParticipanteGrupo.PAGO);
		if (todosPagos) {
			confirmarGrupoEEmitir(grupoId);
		}
	}

	private void confirmarGrupoEEmitir(GrupoCompraId grupoId) {
		grupoCompraServico.confirmarGrupo(grupoId);
		GrupoCompra grupo = grupoCompraServico.obter(grupoId);
		List<ParticipanteGrupo> participantes = grupoCompraServico.participantes(grupoId);
		for (ParticipanteGrupo participante : participantes) {
			for (int n = 0; n < participante.getQuantidade(); n++) {
				Ingresso ingresso = new Ingresso(grupo.getTipoIngressoId(), grupo.getEventoId(),
						participante.getUsuarioId());
				if (participante.isMeiaEntrada()) {
					ingresso.definirMeiaEntrada(participante.getDocumento());
				}
				ingressoServico.salvar(ingresso);
			}
		}
	}

	public void cancelar(GrupoCompraId grupoId, UsuarioId solicitanteId) {
		List<ParticipanteGrupo> participantes = grupoCompraServico.participantes(grupoId);
		grupoCompraServico.cancelarGrupo(grupoId, solicitanteId);
		reembolsarPagos(participantes);
	}

	public void expirarSeNecessario(GrupoCompraId grupoId) {
		GrupoCompra grupo = grupoCompraServico.obter(grupoId);
		if (!grupo.prazoExpirado(LocalDateTime.now())) {
			return;
		}
		List<ParticipanteGrupo> participantes = grupoCompraServico.participantes(grupoId);
		grupoCompraServico.expirarGrupo(grupoId);
		reembolsarPagos(participantes);
	}

	private void reembolsarPagos(List<ParticipanteGrupo> participantes) {
		for (ParticipanteGrupo participante : participantes) {
			if (participante.getStatus() == StatusParticipanteGrupo.PAGO) {
				saldoServico.creditar(participante.getUsuarioId(), participante.getValor());
				transacaoServico.registrar(new Transacao(
						participante.getUsuarioId(), TipoTransacao.REEMBOLSO, participante.getValor(),
						LocalDateTime.now(), UUID.randomUUID()));
				participante.marcarReembolsado();
				participanteGrupoRepositorio.salvar(participante);
			}
		}
	}

	public List<GrupoCompra> listarExpirados() {
		return grupoCompraRepositorio.listarAbertosExpirados(LocalDateTime.now());
	}

	public GrupoCompra obter(GrupoCompraId id) {
		return grupoCompraServico.obter(id);
	}

	public List<GrupoCompra> listarPorEvento(EventoId eventoId) {
		return grupoCompraServico.listarPorEvento(eventoId);
	}

	public List<ParticipanteGrupo> listarParticipantes(GrupoCompraId grupoId) {
		return grupoCompraServico.participantes(grupoId);
	}

	public List<GrupoCompra> listarMeusGrupos(UsuarioId usuarioId) {
		List<GrupoCompra> liderados = grupoCompraServico.listarPorLider(usuarioId);
		List<GrupoCompraId> idsParticipados = participanteGrupoRepositorio.listarPorUsuario(usuarioId).stream()
				.map(ParticipanteGrupo::getGrupoCompraId)
				.toList();

		Set<GrupoCompraId> jaIncluidos = new LinkedHashSet<>();
		List<GrupoCompra> resultado = new ArrayList<>();
		for (GrupoCompra g : liderados) {
			if (jaIncluidos.add(g.getId())) {
				resultado.add(g);
			}
		}
		for (GrupoCompraId id : idsParticipados) {
			if (jaIncluidos.add(id)) {
				resultado.add(grupoCompraServico.obter(id));
			}
		}
		return resultado;
	}
}
