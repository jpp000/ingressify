package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

@Entity
@Table(name = "anuncios_revenda")
public class AnuncioRevendaJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "evento_id", nullable = false)
	private Integer eventoId;

	@Column(name = "vendedor_id", nullable = false)
	private Integer vendedorId;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal preco;

	@Column(name = "comprador_reservado_id")
	private Integer compradorReservadoId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusAnuncio status;

	@Column(name = "correlacao_pagamento")
	private UUID correlacaoPagamento;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "anuncio_ingresso_ids", joinColumns = @JoinColumn(name = "anuncio_id"))
	@Column(name = "ingresso_id", nullable = false)
	private List<UUID> ingressoIds = new ArrayList<>();

	protected AnuncioRevendaJpa() {}

	public static AnuncioRevendaJpa fromDomain(AnuncioRevenda a) {
		var jpa = new AnuncioRevendaJpa();
		if (a.getId() != null) jpa.id = a.getId().getId();
		jpa.eventoId = a.getEventoId().getId();
		jpa.vendedorId = a.getVendedor().getId();
		jpa.preco = a.getPreco().getValor();
		jpa.compradorReservadoId = a.getCompradorReservado() != null ? a.getCompradorReservado().getId() : null;
		jpa.status = a.getStatus();
		jpa.correlacaoPagamento = a.getCorrelacaoPagamento();
		jpa.ingressoIds = a.getIngressoIds().stream().map(IngressoId::getId).toList();
		return jpa;
	}

	public AnuncioRevenda toDomain() {
		List<IngressoId> ids = ingressoIds.stream().map(IngressoId::new).toList();
		UsuarioId comprador = compradorReservadoId != null ? new UsuarioId(compradorReservadoId) : null;
		return new AnuncioRevenda(
				new AnuncioRevendaId(id), ids, new UsuarioId(vendedorId),
				new Dinheiro(preco), new EventoId(eventoId), comprador, status, correlacaoPagamento);
	}

	public Integer getId() { return id; }
	public Integer getEventoId() { return eventoId; }
	public Integer getVendedorId() { return vendedorId; }
	public StatusAnuncio getStatus() { return status; }
	public List<UUID> getIngressoIds() { return ingressoIds; }
}
