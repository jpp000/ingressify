package cesar.rv.ingressify.dominio.marketplace.checkin;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;

public class CheckinServico {

	private final RegistroCheckinRepositorio repositorio;
	private final IngressoRepositorio ingressoRepositorio;

	public CheckinServico(RegistroCheckinRepositorio repositorio, IngressoRepositorio ingressoRepositorio) {
		Validate.notNull(repositorio, "repositorio");
		Validate.notNull(ingressoRepositorio, "ingressoRepositorio");
		this.repositorio = repositorio;
		this.ingressoRepositorio = ingressoRepositorio;
	}

	public RegistroCheckin registrar(Ingresso ingresso, UsuarioId operadorId, LocalDateTime agora) {
		Validate.notNull(ingresso, "ingresso");
		Validate.notNull(operadorId, "operadorId");
		Validate.notNull(agora, "agora");
		if (ingresso.getStatus() != StatusIngresso.ATIVO) {
			throw new IllegalStateException("ingresso deve estar ATIVO para check-in");
		}
		if (ingresso.isBloqueadoPorReembolso()) {
			throw new IllegalStateException("ingresso bloqueado para reembolso");
		}
		ingresso.marcarUtilizado();
		ingressoRepositorio.salvar(ingresso);
		RegistroCheckin reg = new RegistroCheckin(UUID.randomUUID(), ingresso.getId(), ingresso.getEventoId(),
				operadorId, agora);
		repositorio.salvar(reg);
		return reg;
	}
}
