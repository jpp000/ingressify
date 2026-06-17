package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentos;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;

@Entity
@Table(name = "mapas_assentos")
public class MapaAssentosJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "evento_id", nullable = false, unique = true)
    private Integer eventoId;

    @Column(name = "total_linhas", nullable = false)
    private int totalLinhas;

    @Column(name = "total_colunas", nullable = false)
    private int totalColunas;

    protected MapaAssentosJpa() {}

    public static MapaAssentosJpa fromDomain(MapaAssentos m) {
        var jpa = new MapaAssentosJpa();
        if (m.getId() != null) jpa.id = m.getId().getId();
        jpa.eventoId = m.getEventoId().getId();
        jpa.totalLinhas = m.getTotalLinhas();
        jpa.totalColunas = m.getTotalColunas();
        return jpa;
    }

    public MapaAssentos toDomain() {
        return new MapaAssentos(new MapaAssentosId(id), new EventoId(eventoId), totalLinhas, totalColunas);
    }

    public Integer getId() { return id; }
    public Integer getEventoId() { return eventoId; }
}
