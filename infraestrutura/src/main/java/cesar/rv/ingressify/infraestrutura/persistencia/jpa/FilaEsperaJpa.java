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
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.FilaEspera;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;

@Entity
@Table(name = "fila_espera")
public class FilaEsperaJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "mapa_id", nullable = false)
    private Integer mapaId;

    @Column(name = "evento_id", nullable = false)
    private Integer eventoId;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(nullable = false)
    private int posicao;

    @Column(name = "entrada_em", nullable = false)
    private LocalDateTime entradaEm;

    protected FilaEsperaJpa() {}

    public static FilaEsperaJpa fromDomain(FilaEspera f) {
        var jpa = new FilaEsperaJpa();
        if (f.getId() != null) jpa.id = f.getId();
        jpa.mapaId = f.getMapaId().getId();
        jpa.eventoId = f.getEventoId().getId();
        jpa.usuarioId = f.getUsuarioId().getId();
        jpa.posicao = f.getPosicao();
        jpa.entradaEm = f.getEntradaEm();
        return jpa;
    }

    public FilaEspera toDomain() {
        return new FilaEspera(id, new MapaAssentosId(mapaId), new EventoId(eventoId),
                new UsuarioId(usuarioId), posicao, entradaEm);
    }

    public Integer getId() { return id; }
    public Integer getMapaId() { return mapaId; }
    public Integer getUsuarioId() { return usuarioId; }
    public int getPosicao() { return posicao; }
}
