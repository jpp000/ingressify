package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.UUID;

import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "anuncio_revenda")
public class AnuncioRevendaJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "ingresso_id", columnDefinition = "uuid", nullable = false)
	private UUID ingressoId;

	@Column(name = "vendedor_id", nullable = false)
	private Integer vendedorId;

	@Embedded
	private DinheiroJpa preco = new DinheiroJpa();

	@Column(name = "comprador_reservado_id")
	private Integer compradorReservadoId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private StatusAnuncio status;

	@Column(name = "correlacao_pagamento", columnDefinition = "uuid")
	private UUID correlacaoPagamento;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public UUID getIngressoId() {
		return ingressoId;
	}

	public void setIngressoId(UUID ingressoId) {
		this.ingressoId = ingressoId;
	}

	public Integer getVendedorId() {
		return vendedorId;
	}

	public void setVendedorId(Integer vendedorId) {
		this.vendedorId = vendedorId;
	}

	public DinheiroJpa getPreco() {
		return preco;
	}

	public void setPreco(DinheiroJpa preco) {
		this.preco = preco;
	}

	public Integer getCompradorReservadoId() {
		return compradorReservadoId;
	}

	public void setCompradorReservadoId(Integer compradorReservadoId) {
		this.compradorReservadoId = compradorReservadoId;
	}

	public StatusAnuncio getStatus() {
		return status;
	}

	public void setStatus(StatusAnuncio status) {
		this.status = status;
	}

	public UUID getCorrelacaoPagamento() {
		return correlacaoPagamento;
	}

	public void setCorrelacaoPagamento(UUID correlacaoPagamento) {
		this.correlacaoPagamento = correlacaoPagamento;
	}
}
