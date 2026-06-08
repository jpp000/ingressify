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
import cesar.rv.ingressify.dominio.marketplace.sorteio.Sorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusSorteio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

@Entity
@Table(name = "sorteios")
public class SorteioJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "evento_id", nullable = false)
    private Integer eventoId;

    @Column(name = "tipo_ingresso_id", nullable = false)
    private Integer tipoIngressoId;

    @Column(name = "organizador_id", nullable = false)
    private Integer organizadorId;

    @Column(name = "quantidade_ingressos", nullable = false)
    private int quantidadeIngressos;

    @Column(name = "quantidade_lista_espera", nullable = false)
    private int quantidadeListaEspera;

    @Column(name = "prazo_inscricao", nullable = false)
    private LocalDateTime prazoInscricao;

    @Column(name = "prazo_confirmacao_horas", nullable = false)
    private int prazoConfirmacaoHoras;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSorteio status;

    protected SorteioJpa() {}

    public static SorteioJpa fromDomain(Sorteio s) {
        var jpa = new SorteioJpa();
        if (s.getId() != null) jpa.id = s.getId().getId();
        jpa.eventoId = s.getEventoId().getId();
        jpa.tipoIngressoId = s.getTipoIngressoId().getId();
        jpa.organizadorId = s.getOrganizadorId().getId();
        jpa.quantidadeIngressos = s.getQuantidadeIngressos();
        jpa.quantidadeListaEspera = s.getQuantidadeListaEspera();
        jpa.prazoInscricao = s.getPrazoInscricao();
        jpa.prazoConfirmacaoHoras = s.getPrazoConfirmacaoHoras();
        jpa.status = s.getStatus();
        return jpa;
    }

    public Sorteio toDomain() {
        return new Sorteio(
                new SorteioId(id),
                new EventoId(eventoId),
                new TipoIngressoId(tipoIngressoId),
                new UsuarioId(organizadorId),
                quantidadeIngressos,
                quantidadeListaEspera,
                prazoInscricao,
                prazoConfirmacaoHoras,
                status);
    }

    public Integer getId() { return id; }
    public StatusSorteio getStatus() { return status; }
    public Integer getEventoId() { return eventoId; }
}
