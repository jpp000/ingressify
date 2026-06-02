package cesar.rv.ingressify.dominio.marketplace.feed;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class Comentario {

	private ComentarioId id;
	private final PostagemId postagemId;
	private final UsuarioId autorId;
	private final String texto;
	private final LocalDateTime criadaEm;

	public Comentario(PostagemId postagemId, UsuarioId autorId, String texto, LocalDateTime criadaEm) {
		Validate.notNull(postagemId, "postagemId");
		Validate.notNull(autorId, "autorId");
		Validate.notBlank(texto, "texto");
		Validate.notNull(criadaEm, "criadaEm");
		this.postagemId = postagemId;
		this.autorId = autorId;
		this.texto = texto;
		this.criadaEm = criadaEm;
	}

	public Comentario(ComentarioId id, PostagemId postagemId, UsuarioId autorId, String texto,
			LocalDateTime criadaEm) {
		Validate.notNull(id, "id");
		Validate.notNull(postagemId, "postagemId");
		Validate.notNull(autorId, "autorId");
		Validate.notBlank(texto, "texto");
		Validate.notNull(criadaEm, "criadaEm");
		this.id = id;
		this.postagemId = postagemId;
		this.autorId = autorId;
		this.texto = texto;
		this.criadaEm = criadaEm;
	}

	public void atribuirId(ComentarioId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public ComentarioId getId() {
		return id;
	}

	public PostagemId getPostagemId() {
		return postagemId;
	}

	public UsuarioId getAutorId() {
		return autorId;
	}

	public String getTexto() {
		return texto;
	}

	public LocalDateTime getCriadaEm() {
		return criadaEm;
	}
}
