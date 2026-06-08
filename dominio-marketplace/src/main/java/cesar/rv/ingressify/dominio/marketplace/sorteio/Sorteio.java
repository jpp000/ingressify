package cesar.rv.ingressify.dominio.marketplace.sorteio;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.estado.EstadoAguardandoSorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.estado.EstadoCancelado;
import cesar.rv.ingressify.dominio.marketplace.sorteio.estado.EstadoConfigurado;
import cesar.rv.ingressify.dominio.marketplace.sorteio.estado.EstadoEncerrado;
import cesar.rv.ingressify.dominio.marketplace.sorteio.estado.EstadoInscricoesAbertas;
import cesar.rv.ingressify.dominio.marketplace.sorteio.estado.EstadoSorteado;
import cesar.rv.ingressify.dominio.marketplace.sorteio.estado.EstadoSorteio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

/**
 * Agregado raiz do Sorteio. Usa o padrão State para gerenciar transições de ciclo de vida.
 */
public class Sorteio {

    private SorteioId id;
    private final EventoId eventoId;
    private final TipoIngressoId tipoIngressoId;
    private final UsuarioId organizadorId;
    private final int quantidadeIngressos;
    private final int quantidadeListaEspera;
    private final LocalDateTime prazoInscricao;
    private final int prazoConfirmacaoHoras;
    private StatusSorteio status;

    // Padrão State — reconstruído sob demanda a partir do status persistido
    private transient EstadoSorteio estadoAtual;

    public Sorteio(EventoId eventoId, TipoIngressoId tipoIngressoId, UsuarioId organizadorId,
            int quantidadeIngressos, int quantidadeListaEspera,
            LocalDateTime prazoInscricao, int prazoConfirmacaoHoras) {
        Validate.notNull(eventoId, "eventoId");
        Validate.notNull(tipoIngressoId, "tipoIngressoId");
        Validate.notNull(organizadorId, "organizadorId");
        Validate.isTrue(quantidadeIngressos > 0, "quantidade de ingressos deve ser maior que zero");
        Validate.isTrue(quantidadeListaEspera >= 0, "quantidade lista espera deve ser >= 0");
        Validate.notNull(prazoInscricao, "prazoInscricao");
        Validate.isTrue(prazoInscricao.isAfter(LocalDateTime.now()), "prazo de inscrição deve ser futuro");
        Validate.isTrue(prazoConfirmacaoHoras > 0, "prazo de confirmação em horas deve ser maior que zero");
        this.eventoId = eventoId;
        this.tipoIngressoId = tipoIngressoId;
        this.organizadorId = organizadorId;
        this.quantidadeIngressos = quantidadeIngressos;
        this.quantidadeListaEspera = quantidadeListaEspera;
        this.prazoInscricao = prazoInscricao;
        this.prazoConfirmacaoHoras = prazoConfirmacaoHoras;
        this.status = StatusSorteio.CONFIGURADO;
    }

    public Sorteio(SorteioId id, EventoId eventoId, TipoIngressoId tipoIngressoId, UsuarioId organizadorId,
            int quantidadeIngressos, int quantidadeListaEspera,
            LocalDateTime prazoInscricao, int prazoConfirmacaoHoras, StatusSorteio status) {
        Validate.notNull(id, "id");
        Validate.notNull(eventoId, "eventoId");
        Validate.notNull(tipoIngressoId, "tipoIngressoId");
        Validate.notNull(organizadorId, "organizadorId");
        Validate.notNull(prazoInscricao, "prazoInscricao");
        Validate.notNull(status, "status");
        this.id = id;
        this.eventoId = eventoId;
        this.tipoIngressoId = tipoIngressoId;
        this.organizadorId = organizadorId;
        this.quantidadeIngressos = quantidadeIngressos;
        this.quantidadeListaEspera = quantidadeListaEspera;
        this.prazoInscricao = prazoInscricao;
        this.prazoConfirmacaoHoras = prazoConfirmacaoHoras;
        this.status = status;
    }

    public void atribuirId(SorteioId novoId) {
        Validate.notNull(novoId, "novoId");
        this.id = novoId;
    }

    // Delegação ao Estado atual (padrão State)

    public void abrirInscricoes() {
        obterEstado().abrirInscricoes(this);
    }

    public void encerrarInscricoes() {
        obterEstado().encerrarInscricoes(this);
    }

    public void executarSorteio() {
        obterEstado().executarSorteio(this);
    }

    public void encerrar() {
        obterEstado().encerrar(this);
    }

    public void cancelar() {
        obterEstado().cancelar(this);
    }

    /** Chamado pelos estados concretos para efetuar a mudança de status. */
    public void aplicarStatus(StatusSorteio novoStatus) {
        this.status = novoStatus;
        this.estadoAtual = null;
    }

    private EstadoSorteio obterEstado() {
        if (estadoAtual == null) {
            estadoAtual = criarEstado(status);
        }
        return estadoAtual;
    }

    private static EstadoSorteio criarEstado(StatusSorteio s) {
        return switch (s) {
            case CONFIGURADO -> new EstadoConfigurado();
            case INSCRICOES_ABERTAS -> new EstadoInscricoesAbertas();
            case AGUARDANDO_SORTEIO -> new EstadoAguardandoSorteio();
            case SORTEADO -> new EstadoSorteado();
            case ENCERRADO -> new EstadoEncerrado();
            case CANCELADO -> new EstadoCancelado();
        };
    }

    public SorteioId getId() { return id; }
    public EventoId getEventoId() { return eventoId; }
    public TipoIngressoId getTipoIngressoId() { return tipoIngressoId; }
    public UsuarioId getOrganizadorId() { return organizadorId; }
    public int getQuantidadeIngressos() { return quantidadeIngressos; }
    public int getQuantidadeListaEspera() { return quantidadeListaEspera; }
    public LocalDateTime getPrazoInscricao() { return prazoInscricao; }
    public int getPrazoConfirmacaoHoras() { return prazoConfirmacaoHoras; }
    public StatusSorteio getStatus() { return status; }
}
