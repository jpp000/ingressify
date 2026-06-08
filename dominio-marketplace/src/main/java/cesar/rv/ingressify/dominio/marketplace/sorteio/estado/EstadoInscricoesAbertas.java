package cesar.rv.ingressify.dominio.marketplace.sorteio.estado;

import cesar.rv.ingressify.dominio.marketplace.sorteio.Sorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusSorteio;

public class EstadoInscricoesAbertas implements EstadoSorteio {

    @Override
    public void abrirInscricoes(Sorteio sorteio) {
        throw new IllegalStateException("inscrições já estão abertas");
    }

    @Override
    public void encerrarInscricoes(Sorteio sorteio) {
        sorteio.aplicarStatus(StatusSorteio.AGUARDANDO_SORTEIO);
    }

    @Override
    public void executarSorteio(Sorteio sorteio) {
        throw new IllegalStateException("inscrições ainda não foram encerradas");
    }

    @Override
    public void encerrar(Sorteio sorteio) {
        throw new IllegalStateException("sorteio não foi executado");
    }

    @Override
    public void cancelar(Sorteio sorteio) {
        sorteio.aplicarStatus(StatusSorteio.CANCELADO);
    }

    @Override
    public StatusSorteio getStatus() {
        return StatusSorteio.INSCRICOES_ABERTAS;
    }
}
