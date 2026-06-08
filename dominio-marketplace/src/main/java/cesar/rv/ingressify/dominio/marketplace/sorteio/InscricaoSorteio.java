package cesar.rv.ingressify.dominio.marketplace.sorteio;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class InscricaoSorteio {

    private InscricaoSorteioId id;
    private final SorteioId sorteioId;
    private final UsuarioId participanteId;
    private final LocalDateTime inscritoEm;
    private StatusInscricao status;
    private int posicao;

    public InscricaoSorteio(SorteioId sorteioId, UsuarioId participanteId) {
        Validate.notNull(sorteioId, "sorteioId");
        Validate.notNull(participanteId, "participanteId");
        this.sorteioId = sorteioId;
        this.participanteId = participanteId;
        this.inscritoEm = LocalDateTime.now();
        this.status = StatusInscricao.INSCRITO;
        this.posicao = 0;
    }

    public InscricaoSorteio(InscricaoSorteioId id, SorteioId sorteioId, UsuarioId participanteId,
            LocalDateTime inscritoEm, StatusInscricao status, int posicao) {
        Validate.notNull(id, "id");
        Validate.notNull(sorteioId, "sorteioId");
        Validate.notNull(participanteId, "participanteId");
        Validate.notNull(inscritoEm, "inscritoEm");
        Validate.notNull(status, "status");
        this.id = id;
        this.sorteioId = sorteioId;
        this.participanteId = participanteId;
        this.inscritoEm = inscritoEm;
        this.status = status;
        this.posicao = posicao;
    }

    public void atribuirId(InscricaoSorteioId novoId) {
        Validate.notNull(novoId, "novoId");
        this.id = novoId;
    }

    public void contemplar(int posicao) {
        if (status != StatusInscricao.INSCRITO) {
            throw new IllegalStateException("inscrição não está no status INSCRITO");
        }
        this.status = StatusInscricao.CONTEMPLADO;
        this.posicao = posicao;
    }

    public void marcarListaEspera(int posicao) {
        if (status != StatusInscricao.INSCRITO) {
            throw new IllegalStateException("inscrição não está no status INSCRITO");
        }
        this.status = StatusInscricao.LISTA_ESPERA;
        this.posicao = posicao;
    }

    public void confirmar() {
        if (status != StatusInscricao.CONTEMPLADO && status != StatusInscricao.LISTA_ESPERA) {
            throw new IllegalStateException("inscrição não pode ser confirmada no estado atual");
        }
        this.status = StatusInscricao.CONFIRMADO;
    }

    public void expirar() {
        if (status == StatusInscricao.CONFIRMADO || status == StatusInscricao.CANCELADO) {
            throw new IllegalStateException("inscrição não pode ser expirada no estado atual");
        }
        this.status = StatusInscricao.EXPIRADO;
    }

    public void cancelar() {
        if (status == StatusInscricao.CONFIRMADO) {
            throw new IllegalStateException("inscrição já confirmada não pode ser cancelada");
        }
        this.status = StatusInscricao.CANCELADO;
    }

    public InscricaoSorteioId getId() { return id; }
    public SorteioId getSorteioId() { return sorteioId; }
    public UsuarioId getParticipanteId() { return participanteId; }
    public LocalDateTime getInscritoEm() { return inscritoEm; }
    public StatusInscricao getStatus() { return status; }
    public int getPosicao() { return posicao; }
}
