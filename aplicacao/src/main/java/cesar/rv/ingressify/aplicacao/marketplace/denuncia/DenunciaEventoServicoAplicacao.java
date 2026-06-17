package cesar.rv.ingressify.aplicacao.marketplace.denuncia;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.Papel;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioServico;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEvento;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEventoId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.denuncia.MotivoDenunciaEvento;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;

public class DenunciaEventoServicoAplicacao {

	private final DenunciaEventoRepositorio denunciaEventoRepositorio;
	private final EventoRepositorio eventoRepositorio;
	private final UsuarioServico usuarioServico;

	public DenunciaEventoServicoAplicacao(DenunciaEventoRepositorio denunciaEventoRepositorio,
			EventoRepositorio eventoRepositorio, UsuarioServico usuarioServico) {
		Validate.notNull(denunciaEventoRepositorio, "denunciaEventoRepositorio");
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		Validate.notNull(usuarioServico, "usuarioServico");
		this.denunciaEventoRepositorio = denunciaEventoRepositorio;
		this.eventoRepositorio = eventoRepositorio;
		this.usuarioServico = usuarioServico;
	}

	public DenunciaEventoId denunciar(EventoId eventoId, UsuarioId denuncianteId,
			MotivoDenunciaEvento motivo, String descricao) {
		Evento evento = eventoRepositorio.obter(eventoId);
		if (evento.getStatus() == StatusEvento.CANCELADO) {
			throw new IllegalStateException("não é possível denunciar evento cancelado");
		}
		if (denunciaEventoRepositorio.existePorEventoEDenunciante(eventoId, denuncianteId)) {
			throw new IllegalStateException("você já denunciou este evento");
		}
		DenunciaEvento denuncia = new DenunciaEvento(eventoId, denuncianteId, motivo, descricao,
				LocalDateTime.now());
		denunciaEventoRepositorio.salvar(denuncia);
		return denuncia.getId();
	}

	public List<DenunciaEvento> listar(UsuarioId adminId) {
		exigirAdmin(adminId);
		return denunciaEventoRepositorio.pesquisarTodas();
	}

	public void iniciarAnalise(DenunciaEventoId id, UsuarioId adminId) {
		exigirAdmin(adminId);
		DenunciaEvento d = denunciaEventoRepositorio.obter(id);
		d.iniciarAnalise(LocalDateTime.now());
		denunciaEventoRepositorio.salvar(d);
	}

	public void aprovar(DenunciaEventoId id, UsuarioId adminId) {
		exigirAdmin(adminId);
		DenunciaEvento d = denunciaEventoRepositorio.obter(id);
		d.aprovar(LocalDateTime.now());
		denunciaEventoRepositorio.salvar(d);
	}

	public void rejeitar(DenunciaEventoId id, UsuarioId adminId) {
		exigirAdmin(adminId);
		DenunciaEvento d = denunciaEventoRepositorio.obter(id);
		d.rejeitar(LocalDateTime.now());
		denunciaEventoRepositorio.salvar(d);
	}

	private void exigirAdmin(UsuarioId usuarioId) {
		if (!usuarioServico.obter(usuarioId).temPapel(Papel.ADMIN)) {
			throw new IllegalStateException("acesso restrito a administradores");
		}
	}
}
