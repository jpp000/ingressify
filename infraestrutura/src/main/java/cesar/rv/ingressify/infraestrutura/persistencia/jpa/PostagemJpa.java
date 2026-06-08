package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.feed.Postagem;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemId;

@Entity
@Table(name = "postagens")
public class PostagemJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "evento_id", nullable = false)
	private Integer eventoId;

	@Column(name = "autor_id", nullable = false)
	private Integer autorId;

	@Column(nullable = false)
	private String titulo;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String conteudo;

	@Column(name = "imagem_url", columnDefinition = "TEXT")
	private String imagemUrl;

	@Column(nullable = false)
	private boolean fixada;

	@Column(name = "criada_em", nullable = false)
	private LocalDateTime criadaEm;

	protected PostagemJpa() {}

	public static PostagemJpa fromDomain(Postagem p) {
		var jpa = new PostagemJpa();
		if (p.getId() != null) jpa.id = p.getId().getId();
		jpa.eventoId = p.getEventoId().getId();
		jpa.autorId = p.getAutorId().getId();
		jpa.titulo = p.getTitulo();
		jpa.conteudo = p.getConteudo();
		jpa.imagemUrl = p.getImagemUrl();
		jpa.fixada = p.isFixada();
		jpa.criadaEm = p.getCriadaEm();
		return jpa;
	}

	public Postagem toDomain() {
		return new Postagem(new PostagemId(id), new EventoId(eventoId),
				new UsuarioId(autorId), titulo, conteudo, imagemUrl, fixada, criadaEm);
	}

	public Integer getId() { return id; }
	public Integer getEventoId() { return eventoId; }
	public boolean isFixada() { return fixada; }
}
