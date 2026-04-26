package cesar.rv.ingressify.dominio.marketplace.evento;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

public class Evento {

	private EventoId id;
	private String nome;
	private LocalDateTime dataHora;
	private String local;
	private String descricao;
	private StatusEvento status;
	private int capacidade;

	public Evento(String nome, LocalDateTime dataHora, String local, String descricao, int capacidade) {
		Validate.notBlank(nome, "nome");
		Validate.notNull(dataHora, "dataHora");
		Validate.isTrue(dataHora.isAfter(LocalDateTime.now()), "dataHora deve ser futura");
		Validate.notBlank(local, "local");
		Validate.notBlank(descricao, "descricao");
		Validate.isTrue(capacidade > 0, "capacidade deve ser > 0");
		this.nome = nome;
		this.dataHora = dataHora;
		this.local = local;
		this.descricao = descricao;
		this.status = StatusEvento.ATIVO;
		this.capacidade = capacidade;
	}

	public Evento(EventoId id, String nome, LocalDateTime dataHora, String local, String descricao, StatusEvento status,
			int capacidade) {
		Validate.notNull(id, "id");
		Validate.notBlank(nome, "nome");
		Validate.notNull(dataHora, "dataHora");
		Validate.notBlank(local, "local");
		Validate.notBlank(descricao, "descricao");
		Validate.notNull(status, "status");
		Validate.isTrue(capacidade > 0, "capacidade deve ser > 0");
		this.id = id;
		this.nome = nome;
		this.dataHora = dataHora;
		this.local = local;
		this.descricao = descricao;
		this.status = status;
		this.capacidade = capacidade;
	}

	public void atribuirId(EventoId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void atualizar(String nome, LocalDateTime dataHora, String local, String descricao, int capacidade) {
		if (iniciado()) {
			throw new IllegalStateException("evento já iniciado");
		}
		Validate.notBlank(nome, "nome");
		Validate.notNull(dataHora, "dataHora");
		Validate.isTrue(dataHora.isAfter(LocalDateTime.now()), "dataHora deve ser futura");
		Validate.notBlank(local, "local");
		Validate.notBlank(descricao, "descricao");
		Validate.isTrue(capacidade > 0, "capacidade deve ser > 0");
		this.nome = nome;
		this.dataHora = dataHora;
		this.local = local;
		this.descricao = descricao;
		this.capacidade = capacidade;
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
}
