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
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompra;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompraId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.StatusGrupoCompra;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

@Entity
@Table(name = "grupos_compra")
public class GrupoCompraJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "evento_id", nullable = false)
    private Integer eventoId;

    @Column(name = "tipo_ingresso_id", nullable = false)
    private Integer tipoIngressoId;

    @Column(name = "lider_id", nullable = false)
    private Integer liderId;

    @Column(name = "quantidade_total", nullable = false)
    private int quantidadeTotal;

    @Column(name = "prazo_pagamento", nullable = false)
    private LocalDateTime prazoPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusGrupoCompra status;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected GrupoCompraJpa() {}

    public static GrupoCompraJpa fromDomain(GrupoCompra g) {
        var jpa = new GrupoCompraJpa();
        if (g.getId() != null) jpa.id = g.getId().getId();
        jpa.eventoId = g.getEventoId().getId();
        jpa.tipoIngressoId = g.getTipoIngressoId().getId();
        jpa.liderId = g.getLiderId().getId();
        jpa.quantidadeTotal = g.getQuantidadeTotal();
        jpa.prazoPagamento = g.getPrazoPagamento();
        jpa.status = g.getStatus();
        jpa.criadoEm = g.getCriadoEm();
        return jpa;
    }

    public GrupoCompra toDomain() {
        return new GrupoCompra(
                new GrupoCompraId(id),
                new EventoId(eventoId),
                new TipoIngressoId(tipoIngressoId),
                new UsuarioId(liderId),
                quantidadeTotal,
                prazoPagamento,
                status,
                criadoEm);
    }

    public Integer getId() { return id; }
    public Integer getEventoId() { return eventoId; }
    public Integer getLiderId() { return liderId; }
    public StatusGrupoCompra getStatus() { return status; }
}
