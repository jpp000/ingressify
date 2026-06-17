package cesar.rv.ingressify.dominio.marketplace.feed;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public class Postagem {

	private PostagemId id;
	private final EventoId eventoId;
	private final UsuarioId autorId;
	private String titulo;
	private String conteudo;
	private String imagemUrl;
	private boolean fixada;
	private final LocalDateTime criadaEm;

	public Postagem(EventoId eventoId, UsuarioId autorId, String titulo, String conteudo, String imagemUrl,
			boolean fixada, LocalDateTime criadaEm) {
		Validate.notNull(eventoId, "eventoId");
		Validate.notNull(autorId, "autorId");
		Validate.notBlank(titulo, "titulo");
		Validate.notBlank(conteudo, "conteudo");
		Validate.notNull(criadaEm, "criadaEm");
		this.eventoId = eventoId;
		this.autorId = autorId;
		this.titulo = titulo;
		this.conteudo = conteudo;
		this.imagemUrl = imagemUrl;
		this.fixada = fixada;
		this.criadaEm = criadaEm;
	}

	public Postagem(PostagemId id, EventoId eventoId, UsuarioId autorId, String titulo, String conteudo,
			String imagemUrl, boolean fixada, LocalDateTime criadaEm) {
		Validate.notNull(id, "id");
		Validate.notNull(eventoId, "eventoId");
		Validate.notNull(autorId, "autorId");
		Validate.notBlank(titulo, "titulo");
		Validate.notBlank(conteudo, "conteudo");
		Validate.notNull(criadaEm, "criadaEm");
		this.id = id;
		this.eventoId = eventoId;
		this.autorId = autorId;
		this.titulo = titulo;
		this.conteudo = conteudo;
		this.imagemUrl = imagemUrl;
		this.fixada = fixada;
		this.criadaEm = criadaEm;
	}

	public void atribuirId(PostagemId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void editar(String titulo, String conteudo, String imagemUrl) {
		Validate.notBlank(titulo, "titulo");
		Validate.notBlank(conteudo, "conteudo");
		this.titulo = titulo;
		this.conteudo = conteudo;
		this.imagemUrl = imagemUrl;
	}

	public void fixar() {
		this.fixada = true;
	}

	public void desfixar() {
		this.fixada = false;
	}

	public PostagemId getId() {
		return id;
	}

	public EventoId getEventoId() {
		return eventoId;
	}

	public UsuarioId getAutorId() {
		return autorId;
	}

	public String getTitulo() {
		return titulo;
	}

	public String getConteudo() {
		return conteudo;
	}

	public String getImagemUrl() {
		return imagemUrl;
	}

	public boolean isFixada() {
		return fixada;
	}

	public LocalDateTime getCriadaEm() {
		return criadaEm;
	}
}
