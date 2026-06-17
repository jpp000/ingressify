package cesar.rv.ingressify.aplicacao.marketplace.analytics;

import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.Avaliacao;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;

public class AnalyticsServicoAplicacao {

	private final EventoRepositorio eventoRepositorio;
	private final IngressoRepositorio ingressoRepositorio;
	private final TipoIngressoRepositorio tipoIngressoRepositorio;
	private final AvaliacaoRepositorio avaliacaoRepositorio;

	public AnalyticsServicoAplicacao(EventoRepositorio eventoRepositorio,
			IngressoRepositorio ingressoRepositorio,
			TipoIngressoRepositorio tipoIngressoRepositorio,
			AvaliacaoRepositorio avaliacaoRepositorio) {
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		Validate.notNull(ingressoRepositorio, "ingressoRepositorio");
		Validate.notNull(tipoIngressoRepositorio, "tipoIngressoRepositorio");
		Validate.notNull(avaliacaoRepositorio, "avaliacaoRepositorio");
		this.eventoRepositorio = eventoRepositorio;
		this.ingressoRepositorio = ingressoRepositorio;
		this.tipoIngressoRepositorio = tipoIngressoRepositorio;
		this.avaliacaoRepositorio = avaliacaoRepositorio;
	}

	public EventoAnalyticsDto calcular(EventoId eventoId, UsuarioId organizadorId) {
		Evento evento = eventoRepositorio.obter(eventoId);
		if (!evento.getOrganizadorId().equals(organizadorId)) {
			throw new IllegalStateException("acesso negado: evento não pertence ao organizador");
		}

		List<Ingresso> ingressos = ingressoRepositorio.pesquisarPorEvento(eventoId);
		List<TipoIngresso> tipos = tipoIngressoRepositorio.pesquisarPorEvento(eventoId);
		List<Avaliacao> avaliacoes = avaliacaoRepositorio.pesquisarPorEvento(eventoId);

		long vendidos = ingressos.stream()
				.filter(i -> i.getStatus() != StatusIngresso.CANCELADO && i.getStatus() != StatusIngresso.REEMBOLSADO)
				.count();

		long revendidos = ingressos.stream()
				.filter(i -> i.getStatus() == StatusIngresso.REVENDIDO)
				.count();

		int totalCapacidade = tipos.stream().mapToInt(TipoIngresso::getQuantidadeTotal).sum();
		int totalDisponiveis = tipos.stream().mapToInt(TipoIngresso::getQuantidadeDisponivel).sum();

		double taxaOcupacao = totalCapacidade > 0 ? (double) vendidos / totalCapacidade * 100.0 : 0.0;
		double taxaRevenda = vendidos > 0 ? (double) revendidos / vendidos * 100.0 : 0.0;

		double mediaAvaliacao = avaliacoes.isEmpty() ? 0.0
				: avaliacoes.stream().mapToInt(Avaliacao::getNota).average().orElse(0.0);

		return new EventoAnalyticsDto(
				(int) vendidos, (int) revendidos, totalDisponiveis, totalCapacidade,
				taxaOcupacao, taxaRevenda, mediaAvaliacao, avaliacoes.size());
	}
}
