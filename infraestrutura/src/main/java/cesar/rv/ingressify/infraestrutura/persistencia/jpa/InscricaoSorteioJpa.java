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
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteioId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusInscricao;

@Entity
@Table(name = "inscricoes_sorteio")
public class InscricaoSorteioJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sorteio_id", nullable = false)
    private Integer sorteioId;

    @Column(name = "participante_id", nullable = false)
    private Integer participanteId;

    @Column(name = "inscrito_em", nullable = false)
    private LocalDateTime inscritoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusInscricao status;

    @Column(nullable = false)
    private int posicao;

    protected InscricaoSorteioJpa() {}

    public static InscricaoSorteioJpa fromDomain(InscricaoSorteio i) {
        var jpa = new InscricaoSorteioJpa();
        if (i.getId() != null) jpa.id = i.getId().getId();
        jpa.sorteioId = i.getSorteioId().getId();
        jpa.participanteId = i.getParticipanteId().getId();
        jpa.inscritoEm = i.getInscritoEm();
        jpa.status = i.getStatus();
        jpa.posicao = i.getPosicao();
        return jpa;
    }

    public InscricaoSorteio toDomain() {
        return new InscricaoSorteio(
                new InscricaoSorteioId(id),
                new SorteioId(sorteioId),
                new UsuarioId(participanteId),
                inscritoEm,
                status,
                posicao);
    }

    public Integer getId() { return id; }
    public Integer getSorteioId() { return sorteioId; }
    public Integer getParticipanteId() { return participanteId; }
    public StatusInscricao getStatus() { return status; }
}
