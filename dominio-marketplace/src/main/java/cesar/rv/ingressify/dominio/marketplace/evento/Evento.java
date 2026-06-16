package cesar.rv.ingressify.dominio.marketplace.evento;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class Evento {

	private EventoId id;
	private UsuarioId organizadorId;
	private String nome;
	private LocalDateTime dataHora;
	private String local;
	private String descricao;
	private StatusEvento status;
	private int capacidade;
	private int capacidadeNumerada;
	private String imagemCapaUrl;
	private int prazoReembolsoDias;
	private LocalDateTime aberturaPortoes;
	private String categoria;

	// Construtor legado — tudo como ordem de chegada
	public Evento(UsuarioId organizadorId, String nome, LocalDateTime dataHora, String local, String descricao,
			int capacidade, String imagemCapaUrl, int prazoReembolsoDias, LocalDateTime aberturaPortoes) {
		this(organizadorId, nome, dataHora, local, descricao, capacidade, 0, imagemCapaUrl, prazoReembolsoDias,
				aberturaPortoes, null);
	}

	// Construtor legado com categoria — tudo como ordem de chegada
	public Evento(UsuarioId organizadorId, String nome, LocalDateTime dataHora, String local, String descricao,
			int capacidade, String imagemCapaUrl, int prazoReembolsoDias, LocalDateTime aberturaPortoes,
			String categoria) {
		this(organizadorId, nome, dataHora, local, descricao, capacidade, 0, imagemCapaUrl, prazoReembolsoDias,
				aberturaPortoes, categoria);
	}

	// Construtor principal com capacidade numerada
	public Evento(UsuarioId organizadorId, String nome, LocalDateTime dataHora, String local, String descricao,
			int capacidade, int capacidadeNumerada, String imagemCapaUrl, int prazoReembolsoDias,
			LocalDateTime aberturaPortoes, String categoria) {
		Validate.notNull(organizadorId, "organizadorId");
		Validate.notBlank(nome, "nome");
		Validate.notNull(dataHora, "dataHora");
		Validate.isTrue(dataHora.isAfter(LocalDateTime.now()), "dataHora deve ser futura");
		Validate.notBlank(local, "local");
		Validate.isTrue(capacidade > 0, "capacidade deve ser > 0");
		Validate.isTrue(capacidadeNumerada >= 0, "capacidadeNumerada deve ser >= 0");
		Validate.isTrue(capacidadeNumerada <= capacidade,
				"capacidadeNumerada não pode exceder capacidade total");
		Validate.isTrue(prazoReembolsoDias >= 0, "prazoReembolsoDias deve ser >= 0");
		Validate.notNull(aberturaPortoes, "aberturaPortoes");
		this.organizadorId = organizadorId;
		this.nome = nome;
		this.dataHora = dataHora;
		this.local = local;
		this.descricao = descricao;
		this.status = StatusEvento.ATIVO;
		this.capacidade = capacidade;
		this.capacidadeNumerada = capacidadeNumerada;
		this.imagemCapaUrl = imagemCapaUrl;
		this.prazoReembolsoDias = prazoReembolsoDias;
		this.aberturaPortoes = aberturaPortoes;
		this.categoria = categoria;
	}

	// Reconstrução legada (sem capacidadeNumerada — compatível com dados antigos)
	public Evento(EventoId id, UsuarioId organizadorId, String nome, LocalDateTime dataHora, String local,
			String descricao, StatusEvento status, int capacidade, String imagemCapaUrl, int prazoReembolsoDias,
			LocalDateTime aberturaPortoes) {
		this(id, organizadorId, nome, dataHora, local, descricao, status, capacidade, 0, imagemCapaUrl,
				prazoReembolsoDias, aberturaPortoes, null);
	}

	// Reconstrução legada com categoria
	public Evento(EventoId id, UsuarioId organizadorId, String nome, LocalDateTime dataHora, String local,
			String descricao, StatusEvento status, int capacidade, String imagemCapaUrl, int prazoReembolsoDias,
			LocalDateTime aberturaPortoes, String categoria) {
		this(id, organizadorId, nome, dataHora, local, descricao, status, capacidade, 0, imagemCapaUrl,
				prazoReembolsoDias, aberturaPortoes, categoria);
	}

	// Reconstrução completa com capacidade numerada
	public Evento(EventoId id, UsuarioId organizadorId, String nome, LocalDateTime dataHora, String local,
			String descricao, StatusEvento status, int capacidade, int capacidadeNumerada, String imagemCapaUrl,
			int prazoReembolsoDias, LocalDateTime aberturaPortoes, String categoria) {
		Validate.notNull(id, "id");
		Validate.notNull(organizadorId, "organizadorId");
		Validate.notBlank(nome, "nome");
		Validate.notNull(dataHora, "dataHora");
		Validate.notBlank(local, "local");
		Validate.notNull(status, "status");
		Validate.isTrue(capacidade > 0, "capacidade deve ser > 0");
		Validate.isTrue(capacidadeNumerada >= 0, "capacidadeNumerada deve ser >= 0");
		Validate.isTrue(capacidadeNumerada <= capacidade,
				"capacidadeNumerada não pode exceder capacidade total");
		Validate.isTrue(prazoReembolsoDias >= 0, "prazoReembolsoDias deve ser >= 0");
		Validate.notNull(aberturaPortoes, "aberturaPortoes");
		this.id = id;
		this.organizadorId = organizadorId;
		this.nome = nome;
		this.dataHora = dataHora;
		this.local = local;
		this.descricao = descricao;
		this.status = status;
		this.capacidade = capacidade;
		this.capacidadeNumerada = capacidadeNumerada;
		this.imagemCapaUrl = imagemCapaUrl;
		this.prazoReembolsoDias = prazoReembolsoDias;
		this.aberturaPortoes = aberturaPortoes;
		this.categoria = categoria;
	}

	public void atribuirId(EventoId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void atualizar(String nome, LocalDateTime dataHora, String local, String descricao, int capacidade,
			int capacidadeNumerada, String imagemCapaUrl, int prazoReembolsoDias, LocalDateTime aberturaPortoes,
			String categoria) {
		if (iniciado()) {
			throw new IllegalStateException("evento já iniciado");
		}
		Validate.notBlank(nome, "nome");
		Validate.notNull(dataHora, "dataHora");
		Validate.isTrue(dataHora.isAfter(LocalDateTime.now()), "dataHora deve ser futura");
		Validate.notBlank(local, "local");
		Validate.isTrue(capacidade > 0, "capacidade deve ser > 0");
		Validate.isTrue(capacidadeNumerada >= 0, "capacidadeNumerada deve ser >= 0");
		Validate.isTrue(capacidadeNumerada <= capacidade,
				"capacidadeNumerada não pode exceder capacidade total");
		Validate.isTrue(prazoReembolsoDias >= 0, "prazoReembolsoDias deve ser >= 0");
		Validate.notNull(aberturaPortoes, "aberturaPortoes");
		this.nome = nome;
		this.dataHora = dataHora;
		this.local = local;
		this.descricao = descricao;
		this.capacidade = capacidade;
		this.capacidadeNumerada = capacidadeNumerada;
		this.imagemCapaUrl = imagemCapaUrl;
		this.prazoReembolsoDias = prazoReembolsoDias;
		this.aberturaPortoes = aberturaPortoes;
		this.categoria = categoria;
	}

	public void cancelar() {
		this.status = StatusEvento.CANCELADO;
	}

	public boolean iniciado() {
		return dataHora.isBefore(LocalDateTime.now());
	}

	public boolean ativo() {
		return status == StatusEvento.ATIVO && !iniciado();
	}

	public EventoId getId() {
		return id;
	}

	public UsuarioId getOrganizadorId() {
		return organizadorId;
	}

	public String getNome() {
		return nome;
	}

	public LocalDateTime getDataHora() {
		return dataHora;
	}

	public String getLocal() {
		return local;
	}

	public String getDescricao() {
		return descricao;
	}

	public StatusEvento getStatus() {
		return status;
	}

	public int getCapacidade() {
		return capacidade;
	}

	public int getCapacidadeNumerada() {
		return capacidadeNumerada;
	}

	public int getCapacidadeOrdemChegada() {
		return capacidade - capacidadeNumerada;
	}

	public boolean temAssentosNumerados() {
		return capacidadeNumerada > 0;
	}

	public String getImagemCapaUrl() {
		return imagemCapaUrl;
	}

	public int getPrazoReembolsoDias() {
		return prazoReembolsoDias;
	}

	public LocalDateTime getAberturaPortoes() {
		return aberturaPortoes;
	}

	public String getCategoria() {
		return categoria;
	}
}
