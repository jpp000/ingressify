package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.Avaliacao;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

@Entity
@Table(name = "avaliacoes")
public class AvaliacaoJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "evento_id", nullable = false)
	private Integer eventoId;

	@Column(name = "usuario_id", nullable = false)
	private Integer usuarioId;

	@Column(nullable = false)
	private int nota;

	@Column(columnDefinition = "TEXT")
	private String comentario;

	@Column(name = "resposta_organizador", columnDefinition = "TEXT")
	private String respostaOrganizador;

	@Column(name = "criada_em", nullable = false)
	private LocalDateTime criadaEm;

	@Column(name = "atualizada_em")
	private LocalDateTime atualizadaEm;

	protected AvaliacaoJpa() {}

	public static AvaliacaoJpa fromDomain(Avaliacao a) {
		var jpa = new AvaliacaoJpa();
		if (a.getId() != null) jpa.id = a.getId().getId();
		jpa.eventoId = a.getEventoId().getId();
		jpa.usuarioId = a.getUsuarioId().getId();
		jpa.nota = a.getNota();
		jpa.comentario = a.getComentario();
		jpa.respostaOrganizador = a.getRespostaOrganizador();
		jpa.criadaEm = a.getCriadaEm();
		jpa.atualizadaEm = a.getAtualizadaEm();
		return jpa;
	}

	public Avaliacao toDomain() {
		return new Avaliacao(
				new AvaliacaoId(id), new EventoId(eventoId), new UsuarioId(usuarioId),
				nota, comentario, respostaOrganizador, criadaEm, atualizadaEm);
	}

	public Integer getId() { return id; }
	public Integer getEventoId() { return eventoId; }
	public Integer getUsuarioId() { return usuarioId; }
}
