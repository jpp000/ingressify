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

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompraId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupo;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupoId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.StatusParticipanteGrupo;

@Entity
@Table(name = "participantes_grupo_compra")
public class ParticipanteGrupoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "grupo_compra_id", nullable = false)
    private Integer grupoCompraId;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "meia_entrada", nullable = false)
    private boolean meiaEntrada;

    @Column
    private String documento;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusParticipanteGrupo status;

    @Column(name = "pago_em")
    private LocalDateTime pagoEm;

    protected ParticipanteGrupoJpa() {}

    public static ParticipanteGrupoJpa fromDomain(ParticipanteGrupo p) {
        var jpa = new ParticipanteGrupoJpa();
        if (p.getId() != null) jpa.id = p.getId().getId();
        jpa.grupoCompraId = p.getGrupoCompraId().getId();
        jpa.usuarioId = p.getUsuarioId().getId();
        jpa.quantidade = p.getQuantidade();
        jpa.meiaEntrada = p.isMeiaEntrada();
        jpa.documento = p.getDocumento();
        jpa.valor = p.getValor().getValor();
        jpa.status = p.getStatus();
        jpa.pagoEm = p.getPagoEm();
        return jpa;
    }

    public ParticipanteGrupo toDomain() {
        return new ParticipanteGrupo(
                new ParticipanteGrupoId(id),
                new GrupoCompraId(grupoCompraId),
                new UsuarioId(usuarioId),
                quantidade,
                meiaEntrada,
                documento,
                new Dinheiro(valor),
                status,
                pagoEm);
    }

    public Integer getId() { return id; }
    public Integer getGrupoCompraId() { return grupoCompraId; }
    public Integer getUsuarioId() { return usuarioId; }
    public StatusParticipanteGrupo getStatus() { return status; }
}
