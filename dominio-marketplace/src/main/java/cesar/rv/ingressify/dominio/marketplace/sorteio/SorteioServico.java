package cesar.rv.ingressify.dominio.marketplace.sorteio;

import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public class SorteioServico {

    private final SorteioRepositorio sorteioRepositorio;
    private final InscricaoSorteioRepositorio inscricaoRepositorio;

    public SorteioServico(SorteioRepositorio sorteioRepositorio,
            InscricaoSorteioRepositorio inscricaoRepositorio) {
        Validate.notNull(sorteioRepositorio, "sorteioRepositorio");
        Validate.notNull(inscricaoRepositorio, "inscricaoRepositorio");
        this.sorteioRepositorio = sorteioRepositorio;
        this.inscricaoRepositorio = inscricaoRepositorio;
    }

    public Sorteio salvar(Sorteio sorteio) {
        sorteioRepositorio.salvar(sorteio);
        return sorteio;
    }

    public Sorteio obter(SorteioId id) {
        return sorteioRepositorio.obter(id);
    }

    public List<Sorteio> listarPorEvento(EventoId eventoId) {
        return sorteioRepositorio.pesquisarPorEvento(eventoId);
    }

    public InscricaoSorteio inscrever(SorteioId sorteioId, UsuarioId participanteId) {
        Validate.notNull(sorteioId, "sorteioId");
        Validate.notNull(participanteId, "participanteId");

        Sorteio sorteio = sorteioRepositorio.obter(sorteioId);
        if (sorteio.getStatus() != StatusSorteio.INSCRICOES_ABERTAS) {
            throw new IllegalStateException("inscrições não estão abertas para este sorteio");
        }
        if (inscricaoRepositorio.existeInscricaoAtiva(sorteioId, participanteId)) {
            throw new IllegalStateException("participante já inscrito neste sorteio");
        }
        InscricaoSorteio inscricao = new InscricaoSorteio(sorteioId, participanteId);
        inscricaoRepositorio.salvar(inscricao);
        return inscricao;
    }

    public void abrirInscricoes(SorteioId sorteioId, UsuarioId solicitanteId) {
        Sorteio sorteio = sorteioRepositorio.obter(sorteioId);
        validarOrganizador(sorteio, solicitanteId);
        sorteio.abrirInscricoes();
        sorteioRepositorio.salvar(sorteio);
    }

    public void encerrarInscricoes(SorteioId sorteioId, UsuarioId solicitanteId) {
        Sorteio sorteio = sorteioRepositorio.obter(sorteioId);
        validarOrganizador(sorteio, solicitanteId);
        sorteio.encerrarInscricoes();
        sorteioRepositorio.salvar(sorteio);
    }

    public List<InscricaoSorteio> listarInscricoes(SorteioId sorteioId) {
        return inscricaoRepositorio.pesquisarPorSorteio(sorteioId);
    }

    public void salvarInscricao(InscricaoSorteio inscricao) {
        inscricaoRepositorio.salvar(inscricao);
    }

    public void encerrar(SorteioId sorteioId) {
        Sorteio sorteio = sorteioRepositorio.obter(sorteioId);
        sorteio.encerrar();
        sorteioRepositorio.salvar(sorteio);
    }

    public void cancelar(SorteioId sorteioId, UsuarioId solicitanteId) {
        Sorteio sorteio = sorteioRepositorio.obter(sorteioId);
        validarOrganizador(sorteio, solicitanteId);
        sorteio.cancelar();
        sorteioRepositorio.salvar(sorteio);

        List<InscricaoSorteio> inscricoes = inscricaoRepositorio.pesquisarPorSorteio(sorteioId);
        for (InscricaoSorteio inscricao : inscricoes) {
            if (inscricao.getStatus() == StatusInscricao.INSCRITO
                    || inscricao.getStatus() == StatusInscricao.CONTEMPLADO
                    || inscricao.getStatus() == StatusInscricao.LISTA_ESPERA) {
                inscricao.cancelar();
                inscricaoRepositorio.salvar(inscricao);
            }
        }
    }

    private void validarOrganizador(Sorteio sorteio, UsuarioId solicitanteId) {
        if (!sorteio.getOrganizadorId().equals(solicitanteId)) {
            throw new IllegalStateException("apenas o organizador pode realizar esta operação");
        }
    }
}
