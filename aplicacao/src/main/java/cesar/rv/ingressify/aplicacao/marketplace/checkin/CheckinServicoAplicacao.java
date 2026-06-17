package cesar.rv.ingressify.aplicacao.marketplace.checkin;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.Papel;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.checkin.CheckinServico;
import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckin;
import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckinRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;

public class CheckinServicoAplicacao {

	private final CheckinServico checkinServico;
	private final IngressoRepositorio ingressoRepositorio;
	private final EventoRepositorio eventoRepositorio;
	private final UsuarioRepositorio usuarioRepositorio;
	private final RegistroCheckinRepositorio registroCheckinRepositorio;

	public CheckinServicoAplicacao(CheckinServico checkinServico, IngressoRepositorio ingressoRepositorio,
			EventoRepositorio eventoRepositorio, UsuarioRepositorio usuarioRepositorio,
			RegistroCheckinRepositorio registroCheckinRepositorio) {
		Validate.notNull(checkinServico, "checkinServico");
		Validate.notNull(ingressoRepositorio, "ingressoRepositorio");
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		Validate.notNull(usuarioRepositorio, "usuarioRepositorio");
		Validate.notNull(registroCheckinRepositorio, "registroCheckinRepositorio");
		this.checkinServico = checkinServico;
		this.ingressoRepositorio = ingressoRepositorio;
		this.eventoRepositorio = eventoRepositorio;
		this.usuarioRepositorio = usuarioRepositorio;
		this.registroCheckinRepositorio = registroCheckinRepositorio;
	}

	public RegistroCheckin realizarCheckin(String codigoQr, UsuarioId operadorId) {
		Ingresso ingresso = ingressoRepositorio.obterPorCodigo(codigoQr);
		Evento evento = eventoRepositorio.obter(ingresso.getEventoId());
		var operador = usuarioRepositorio.obter(operadorId);
		boolean pode = operador.temPapel(Papel.ADMIN)
				|| operador.temPapel(Papel.OPERADOR_PORTA)
				|| (operador.temPapel(Papel.ORGANIZADOR) && evento.getOrganizadorId().equals(operadorId));
		if (!pode) {
			throw new IllegalStateException("operador sem permissão para check-in");
		}
		LocalDateTime agora = LocalDateTime.now();
		if (agora.isBefore(evento.getAberturaPortoes())) {
			throw new IllegalStateException("portões ainda não abertos");
		}
		return checkinServico.registrar(ingresso, operadorId, agora);
	}

	public IngressoId buscarIngressoPorCodigo(String codigo) {
		return ingressoRepositorio.obterPorCodigo(codigo).getId();
	}

	public List<RegistroCheckin> relatorio(EventoId eventoId) {
		return registroCheckinRepositorio.pesquisarPorEvento(eventoId);
	}
}
