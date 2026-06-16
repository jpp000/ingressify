package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;

@Entity
@Table(name = "eventos")
public class EventoJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "organizador_id", nullable = false)
	private Integer organizadorId;

	@Column(nullable = false)
	private String nome;

	@Column(name = "data_hora", nullable = false)
	private LocalDateTime dataHora;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String local;

	@Column(columnDefinition = "TEXT")
	private String descricao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusEvento status;

	@Column(nullable = false)
	private int capacidade;

	@Column(name = "capacidade_numerada", nullable = false, columnDefinition = "int not null default 0")
	private int capacidadeNumerada;

	@Column(name = "imagem_capa_url", columnDefinition = "TEXT")
	private String imagemCapaUrl;

	@Column(name = "prazo_reembolso_dias", nullable = false)
	private int prazoReembolsoDias;

	@Column(name = "abertura_portoes", nullable = false)
	private LocalDateTime aberturaPortoes;

	@Column
	private String categoria;

	protected EventoJpa() {}

	public static EventoJpa fromDomain(Evento e) {
		var jpa = new EventoJpa();
		if (e.getId() != null) jpa.id = e.getId().getId();
		jpa.organizadorId = e.getOrganizadorId().getId();
		jpa.nome = e.getNome();
		jpa.dataHora = e.getDataHora();
		jpa.local = e.getLocal();
		jpa.descricao = e.getDescricao();
		jpa.status = e.getStatus();
		jpa.capacidade = e.getCapacidade();
		jpa.capacidadeNumerada = e.getCapacidadeNumerada();
		jpa.imagemCapaUrl = e.getImagemCapaUrl();
		jpa.prazoReembolsoDias = e.getPrazoReembolsoDias();
		jpa.aberturaPortoes = e.getAberturaPortoes();
		jpa.categoria = e.getCategoria();
		return jpa;
	}

	public Evento toDomain() {
		return new Evento(
				new EventoId(id), new UsuarioId(organizadorId), nome, dataHora,
				local, descricao, status, capacidade, capacidadeNumerada, imagemCapaUrl,
				prazoReembolsoDias, aberturaPortoes, categoria);
	}

	public Integer getId() { return id; }
	public Integer getOrganizadorId() { return organizadorId; }
	public StatusEvento getStatus() { return status; }
}
