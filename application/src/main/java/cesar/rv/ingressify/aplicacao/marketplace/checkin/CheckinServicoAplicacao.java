package cesar.rv.ingressify.aplicacao.marketplace.checkin;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.Papel;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.checkin.CheckinServico;
import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckin;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;

public class CheckinServicoAplicacao {

	private final CheckinServico checkinServico;
	private final IngressoRepositorio ingressoRepositorio;
	private final EventoRepositorio eventoRepositorio;
	private final UsuarioRepositorio usuarioRepositorio;

	public CheckinServicoAplicacao(CheckinServico checkinServico, IngressoRepositorio ingressoRepositorio,
			EventoRepositorio eventoRepositorio, UsuarioRepositorio usuarioRepositorio) {
		Validate.notNull(checkinServico, "checkinServico");
		Validate.notNull(ingressoRepositorio, "ingressoRepositorio");
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		Validate.notNull(usuarioRepositorio, "usuarioRepositorio");
		this.checkinServico = checkinServico;
		this.ingressoRepositorio = ingressoRepositorio;
		this.eventoRepositorio = eventoRepositorio;
		this.usuarioRepositorio = usuarioRepositorio;
	}

	public RegistroCheckin realizarCheckin(String codigoQr, UsuarioId operadorId) {
		IngressoId ingressoId = new IngressoId(UUID.fromString(codigoQr.trim()));
		Ingresso ingresso = ingressoRepositorio.obter(ingressoId);
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
		IngressoId id = new IngressoId(UUID.fromString(codigo.trim()));
		ingressoRepositorio.obter(id);
		return id;
	}
}
