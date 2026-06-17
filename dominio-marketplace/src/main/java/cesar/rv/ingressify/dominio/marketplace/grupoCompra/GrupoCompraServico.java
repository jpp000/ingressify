package cesar.rv.ingressify.dominio.marketplace.grupoCompra;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;

public class GrupoCompraServico {

	private final GrupoCompraRepositorio grupoRepositorio;
	private final ParticipanteGrupoRepositorio participanteRepositorio;
	private final TipoIngressoServico tipoIngressoServico;
	private final EventoRepositorio eventoRepositorio;

	public GrupoCompraServico(GrupoCompraRepositorio grupoRepositorio,
			ParticipanteGrupoRepositorio participanteRepositorio, TipoIngressoServico tipoIngressoServico,
			EventoRepositorio eventoRepositorio) {
		Validate.notNull(grupoRepositorio, "grupoRepositorio");
		Validate.notNull(participanteRepositorio, "participanteRepositorio");
		Validate.notNull(tipoIngressoServico, "tipoIngressoServico");
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		this.grupoRepositorio = grupoRepositorio;
		this.participanteRepositorio = participanteRepositorio;
		this.tipoIngressoServico = tipoIngressoServico;
		this.eventoRepositorio = eventoRepositorio;
	}

	public GrupoCompra criar(EventoId eventoId, TipoIngressoId tipoIngressoId, UsuarioId liderId,
			List<ParticipanteGrupoEntrada> entradas, LocalDateTime prazoPagamento) {
		Validate.notEmpty(entradas, "entradas");
		Validate.notNull(prazoPagamento, "prazoPagamento");

		Evento evento = eventoRepositorio.obter(eventoId);
		if (!evento.ativo()) {
			throw new IllegalStateException("evento não está ativo");
		}

		TipoIngresso tipo = tipoIngressoServico.obter(tipoIngressoId);
		if (!tipo.getEventoId().equals(eventoId)) {
			throw new IllegalArgumentException("tipo de ingresso não pertence ao evento informado");
		}

		if (!prazoPagamento.isAfter(LocalDateTime.now())) {
			throw new IllegalArgumentException("prazoPagamento deve ser futuro");
		}
		if (!prazoPagamento.isBefore(evento.getDataHora())) {
			throw new IllegalArgumentException("prazoPagamento deve ser antes da data do evento");
		}

		// Validação completa antes de qualquer mutação: o projeto não usa @Transactional,
		// então qualquer reserva de estoque feita aqui precisa ser garantida de sucesso.
		Set<UsuarioId> usuariosUnicos = new HashSet<>();
		boolean liderPresente = false;
		int totalQuantidade = 0;
		int totalMeia = 0;
		for (ParticipanteGrupoEntrada entrada : entradas) {
			Validate.isTrue(entrada.quantidade() > 0, "quantidade deve ser > 0");
			UsuarioId usuarioId = new UsuarioId(entrada.usuarioId());
			if (!usuariosUnicos.add(usuarioId)) {
				throw new IllegalArgumentException(
						"usuário duplicado na lista de participantes: " + entrada.usuarioId());
			}
			if (entrada.meiaEntrada() && (entrada.documento() == null || entrada.documento().isBlank())) {
				throw new IllegalArgumentException(
						"documento obrigatório para meia-entrada (usuário " + entrada.usuarioId() + ")");
			}
			if (usuarioId.equals(liderId)) {
				liderPresente = true;
			}
			totalQuantidade += entrada.quantidade();
			if (entrada.meiaEntrada()) {
				totalMeia += entrada.quantidade();
			}
		}
		if (!liderPresente) {
			throw new IllegalArgumentException("líder deve constar na lista de participantes");
		}
		if (tipo.getQuantidadeDisponivel() < totalQuantidade) {
			throw new IllegalStateException("quantidade indisponível para o grupo");
		}
		if (totalMeia > 0 && tipo.getCotaMeiaDisponivel() < totalMeia) {
			throw new IllegalStateException("cota de meia-entrada insuficiente para o grupo");
		}

		GrupoCompra grupo = new GrupoCompra(eventoId, tipoIngressoId, liderId, totalQuantidade, prazoPagamento);
		grupoRepositorio.salvar(grupo);

		for (ParticipanteGrupoEntrada entrada : entradas) {
			if (entrada.meiaEntrada()) {
				tipo.reservarMeia(entrada.quantidade());
			} else {
				tipo.reservar(entrada.quantidade());
			}
			Dinheiro precoUnitario = entrada.meiaEntrada() ? tipo.precoMeia() : tipo.getPreco();
			Dinheiro valor = precoUnitario.multiplicar(entrada.quantidade());
			ParticipanteGrupo participante = new ParticipanteGrupo(grupo.getId(), new UsuarioId(entrada.usuarioId()),
					entrada.quantidade(), entrada.meiaEntrada(), entrada.documento(), valor);
			participanteRepositorio.salvar(participante);
		}

		tipoIngressoServico.salvar(tipo);
		return grupo;
	}

	public GrupoCompra obter(GrupoCompraId id) {
		return grupoRepositorio.obter(id);
	}

	public List<GrupoCompra> listarPorEvento(EventoId eventoId) {
		return grupoRepositorio.listarPorEvento(eventoId);
	}

	public List<GrupoCompra> listarPorLider(UsuarioId liderId) {
		return grupoRepositorio.listarPorLider(liderId);
	}

	public List<ParticipanteGrupo> participantes(GrupoCompraId grupoCompraId) {
		return participanteRepositorio.listarPorGrupo(grupoCompraId);
	}

	public void confirmarPagamentoParticipante(GrupoCompraId grupoCompraId, UsuarioId usuarioId) {
		GrupoCompra grupo = grupoRepositorio.obter(grupoCompraId);
		if (grupo.getStatus() != StatusGrupoCompra.ABERTO) {
			throw new IllegalStateException("grupo de compra não está ABERTO");
		}
		if (grupo.prazoExpirado(LocalDateTime.now())) {
			throw new IllegalStateException("prazo de pagamento do grupo expirou");
		}
		ParticipanteGrupo participante = participanteRepositorio.buscarPorGrupoEUsuario(grupoCompraId, usuarioId)
				.orElseThrow(() -> new IllegalArgumentException("participante não encontrado no grupo"));
		participante.marcarPago();
		participanteRepositorio.salvar(participante);
	}

	public void confirmarGrupo(GrupoCompraId grupoCompraId) {
		GrupoCompra grupo = grupoRepositorio.obter(grupoCompraId);
		List<ParticipanteGrupo> participantes = participanteRepositorio.listarPorGrupo(grupoCompraId);
		boolean todosPagos = participantes.stream().allMatch(p -> p.getStatus() == StatusParticipanteGrupo.PAGO);
		if (!todosPagos) {
			throw new IllegalStateException("nem todos os participantes pagaram sua parte");
		}
		grupo.confirmar();
		grupoRepositorio.salvar(grupo);
	}

	public void expirarGrupo(GrupoCompraId grupoCompraId) {
		GrupoCompra grupo = grupoRepositorio.obter(grupoCompraId);
		if (!grupo.prazoExpirado(LocalDateTime.now())) {
			throw new IllegalStateException("prazo de pagamento do grupo ainda não expirou");
		}
		devolverEstoque(grupo);
		grupo.expirar();
		grupoRepositorio.salvar(grupo);
	}

	public void cancelarGrupo(GrupoCompraId grupoCompraId, UsuarioId solicitanteId) {
		GrupoCompra grupo = grupoRepositorio.obter(grupoCompraId);
		if (!grupo.getLiderId().equals(solicitanteId)) {
			throw new IllegalStateException("apenas o líder pode cancelar o grupo de compra");
		}
		if (grupo.getStatus() != StatusGrupoCompra.ABERTO) {
			throw new IllegalStateException("grupo de compra não está ABERTO");
		}
		devolverEstoque(grupo);
		grupo.cancelar();
		grupoRepositorio.salvar(grupo);
	}

	private void devolverEstoque(GrupoCompra grupo) {
		TipoIngresso tipo = tipoIngressoServico.obter(grupo.getTipoIngressoId());
		List<ParticipanteGrupo> participantes = participanteRepositorio.listarPorGrupo(grupo.getId());
		for (ParticipanteGrupo participante : participantes) {
			if (participante.isMeiaEntrada()) {
				tipo.devolverMeia(participante.getQuantidade());
			} else {
				tipo.devolver(participante.getQuantidade());
			}
		}
		tipoIngressoServico.salvar(tipo);
	}
}
