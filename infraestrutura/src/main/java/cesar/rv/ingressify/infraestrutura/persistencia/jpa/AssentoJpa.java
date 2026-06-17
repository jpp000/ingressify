package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.math.BigDecimal;
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
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.Assento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.AssentoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.StatusAssento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.TipoAssento;

@Entity
@Table(name = "assentos")
public class AssentoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "mapa_id", nullable = false)
    private Integer mapaId;

    @Column(name = "evento_id", nullable = false)
    private Integer eventoId;

    @Column(nullable = false)
    private String secao;

    @Column(nullable = false)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAssento tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAssento status;

    @Column(name = "reservado_por")
    private Integer reservadoPor;

    @Column(name = "reservado_ate")
    private LocalDateTime reservadoAte;

    protected AssentoJpa() {}

    public static AssentoJpa fromDomain(Assento a) {
        var jpa = new AssentoJpa();
        if (a.getId() != null) jpa.id = a.getId().getId();
        jpa.mapaId = a.getMapaId().getId();
        jpa.eventoId = a.getEventoId().getId();
        jpa.secao = a.getSecao();
        jpa.codigo = a.getCodigo();
        jpa.tipo = a.getTipo();
        jpa.preco = a.getPreco();
        jpa.status = a.getStatus();
        jpa.reservadoPor = a.getReservadoPor() != null ? a.getReservadoPor().getId() : null;
        jpa.reservadoAte = a.getReservadoAte();
        return jpa;
    }

    public Assento toDomain() {
        UsuarioId reservante = reservadoPor != null ? new UsuarioId(reservadoPor) : null;
        return new Assento(
                new AssentoId(id), new MapaAssentosId(mapaId), new EventoId(eventoId),
                secao, codigo, tipo, preco, status, reservante, reservadoAte);
    }

    public Integer getId() { return id; }
    public Integer getMapaId() { return mapaId; }
    public Integer getEventoId() { return eventoId; }
    public StatusAssento getStatus() { return status; }
}
