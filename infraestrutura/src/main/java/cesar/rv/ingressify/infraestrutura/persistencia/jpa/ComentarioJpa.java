package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.feed.Comentario;
import cesar.rv.ingressify.dominio.marketplace.feed.ComentarioId;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemId;

@Entity
@Table(name = "comentarios")
public class ComentarioJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "postagem_id", nullable = false)
	private Integer postagemId;

	@Column(name = "autor_id", nullable = false)
	private Integer autorId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String texto;

	@Column(name = "criada_em", nullable = false)
	private LocalDateTime criadaEm;

	protected ComentarioJpa() {}

	public static ComentarioJpa fromDomain(Comentario c) {
		var jpa = new ComentarioJpa();
		if (c.getId() != null) jpa.id = c.getId().getId();
		jpa.postagemId = c.getPostagemId().getId();
		jpa.autorId = c.getAutorId().getId();
		jpa.texto = c.getTexto();
		jpa.criadaEm = c.getCriadaEm();
		return jpa;
	}

	public Comentario toDomain() {
		return new Comentario(new ComentarioId(id), new PostagemId(postagemId),
				new UsuarioId(autorId), texto, criadaEm);
	}

	public Integer getId() { return id; }
	public Integer getPostagemId() { return postagemId; }
}
