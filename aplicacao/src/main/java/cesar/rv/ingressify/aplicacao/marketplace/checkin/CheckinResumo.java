package cesar.rv.ingressify.aplicacao.marketplace.checkin;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

public interface CheckinResumo {

	IngressoId getIngressoId();

	EventoId getEventoId();

	UsuarioId getOperadorId();

	LocalDateTime getDataHora();
}
