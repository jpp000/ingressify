package cesar.rv.ingressify.dominio.marketplace.avaliacao;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public class Avaliacao {

	private AvaliacaoId id;
	private final EventoId eventoId;
	private final UsuarioId usuarioId;
	private int nota;
	private String comentario;
	private String respostaOrganizador;
	private final LocalDateTime criadaEm;
	private LocalDateTime atualizadaEm;

	public Avaliacao(EventoId eventoId, UsuarioId usuarioId, int nota, String comentario, LocalDateTime criadaEm) {
		Validate.notNull(eventoId, "eventoId");
		Validate.notNull(usuarioId, "usuarioId");
		Validate.isTrue(nota >= 1 && nota <= 5, "nota entre 1 e 5");
		Validate.notNull(criadaEm, "criadaEm");
		this.eventoId = eventoId;
		this.usuarioId = usuarioId;
		this.nota = nota;
		this.comentario = comentario;
		this.criadaEm = criadaEm;
	}

	public Avaliacao(AvaliacaoId id, EventoId eventoId, UsuarioId usuarioId, int nota, String comentario,
			String respostaOrganizador, LocalDateTime criadaEm, LocalDateTime atualizadaEm) {
		Validate.notNull(id, "id");
		Validate.notNull(eventoId, "eventoId");
		Validate.notNull(usuarioId, "usuarioId");
		Validate.isTrue(nota >= 1 && nota <= 5, "nota entre 1 e 5");
		Validate.notNull(criadaEm, "criadaEm");
		this.id = id;
		this.eventoId = eventoId;
		this.usuarioId = usuarioId;
		this.nota = nota;
		this.comentario = comentario;
		this.respostaOrganizador = respostaOrganizador;
		this.criadaEm = criadaEm;
		this.atualizadaEm = atualizadaEm;
	}

	public void atribuirId(AvaliacaoId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void editar(int nota, String comentario, LocalDateTime agora) {
		Validate.isTrue(nota >= 1 && nota <= 5, "nota entre 1 e 5");
		this.nota = nota;
		this.comentario = comentario;
		this.atualizadaEm = agora;
	}

	public void responder(String texto, LocalDateTime agora) {
		Validate.notBlank(texto, "texto");
		this.respostaOrganizador = texto;
		this.atualizadaEm = agora;
	}

	public AvaliacaoId getId() {
		return id;
	}

	public EventoId getEventoId() {
		return eventoId;
	}

	public UsuarioId getUsuarioId() {
		return usuarioId;
	}

	public int getNota() {
		return nota;
	}

	public String getComentario() {
		return comentario;
	}

	public String getRespostaOrganizador() {
		return respostaOrganizador;
	}

	public LocalDateTime getCriadaEm() {
		return criadaEm;
	}

	public LocalDateTime getAtualizadaEm() {
		return atualizadaEm;
	}
}
