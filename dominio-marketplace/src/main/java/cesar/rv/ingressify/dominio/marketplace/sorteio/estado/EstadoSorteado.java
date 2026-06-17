package cesar.rv.ingressify.dominio.marketplace.sorteio.estado;

import cesar.rv.ingressify.dominio.marketplace.sorteio.Sorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusSorteio;

public class EstadoSorteado implements EstadoSorteio {

    @Override
    public void abrirInscricoes(Sorteio sorteio) {
        throw new IllegalStateException("sorteio já foi executado");
    }

    @Override
    public void encerrarInscricoes(Sorteio sorteio) {
        throw new IllegalStateException("sorteio já foi executado");
    }

    @Override
    public void executarSorteio(Sorteio sorteio) {
        throw new IllegalStateException("sorteio já foi executado");
    }

    @Override
    public void encerrar(Sorteio sorteio) {
        sorteio.aplicarStatus(StatusSorteio.ENCERRADO);
    }

    @Override
    public void cancelar(Sorteio sorteio) {
        sorteio.aplicarStatus(StatusSorteio.CANCELADO);
    }

    @Override
    public StatusSorteio getStatus() {
        return StatusSorteio.SORTEADO;
    }
}
