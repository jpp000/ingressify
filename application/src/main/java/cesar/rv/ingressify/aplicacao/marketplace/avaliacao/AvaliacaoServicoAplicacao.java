package cesar.rv.ingressify.aplicacao.marketplace.avaliacao;

import java.time.LocalDateTime;

import java.util.Optional;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.Avaliacao;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoId;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoServico;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;

public class AvaliacaoServicoAplicacao {

	private final AvaliacaoServico avaliacaoServico;
	private final AvaliacaoRepositorio avaliacaoRepositorio;
	private final EventoRepositorio eventoRepositorio;
	private final IngressoRepositorio ingressoRepositorio;

	public AvaliacaoServicoAplicacao(AvaliacaoServico avaliacaoServico, AvaliacaoRepositorio avaliacaoRepositorio,
			EventoRepositorio eventoRepositorio, IngressoRepositorio ingressoRepositorio) {
		Validate.notNull(avaliacaoServico, "avaliacaoServico");
		Validate.notNull(avaliacaoRepositorio, "avaliacaoRepositorio");
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		Validate.notNull(ingressoRepositorio, "ingressoRepositorio");
		this.avaliacaoServico = avaliacaoServico;
		this.avaliacaoRepositorio = avaliacaoRepositorio;
		this.eventoRepositorio = eventoRepositorio;
		this.ingressoRepositorio = ingressoRepositorio;
	}

	public void avaliar(EventoId eventoId, UsuarioId usuarioId, int nota, String comentario) {
		Evento evento = eventoRepositorio.obter(eventoId);
		if (evento.getStatus() == StatusEvento.CANCELADO) {
			throw new IllegalStateException("evento cancelado");
		}
		LocalDateTime agora = LocalDateTime.now();
		if (!evento.getDataHora().isBefore(agora)) {
			throw new IllegalStateException("evento ainda não ocorreu");
		}
		if (agora.isAfter(evento.getDataHora().plusDays(30))) {
			throw new IllegalStateException("prazo de avaliação encerrado");
		}
		boolean temIngresso = ingressoRepositorio.pesquisarPorEvento(eventoId).stream()
				.anyMatch(i -> i.getProprietario().equals(usuarioId));
		if (!temIngresso) {
			throw new IllegalStateException("usuário sem ingresso no evento");
		}
		Optional<Avaliacao> existente = avaliacaoRepositorio.buscarPorEventoEUsuario(eventoId, usuarioId);
		if (existente.isPresent()) {
			Avaliacao a = existente.get();
			a.editar(nota, comentario, agora);
			avaliacaoServico.salvar(a);
		} else {
			Avaliacao nova = new Avaliacao(eventoId, usuarioId, nota, comentario, agora);
			avaliacaoServico.salvar(nova);
		}
	}

	public void responder(AvaliacaoId avaliacaoId, UsuarioId organizadorId, String texto) {
		Avaliacao a = avaliacaoServico.obter(avaliacaoId);
		Evento evento = eventoRepositorio.obter(a.getEventoId());
		if (!evento.getOrganizadorId().equals(organizadorId)) {
			throw new IllegalStateException("organizador não é dono do evento");
		}
		avaliacaoServico.responder(avaliacaoId, texto, LocalDateTime.now());
	}
}
