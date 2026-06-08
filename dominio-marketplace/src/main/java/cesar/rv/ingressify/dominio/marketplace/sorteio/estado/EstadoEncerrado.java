package cesar.rv.ingressify.dominio.marketplace.sorteio.estado;

import cesar.rv.ingressify.dominio.marketplace.sorteio.Sorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusSorteio;

public class EstadoEncerrado implements EstadoSorteio {

    @Override
    public void abrirInscricoes(Sorteio sorteio) {
        throw new IllegalStateException("sorteio já encerrado");
    }

    @Override
    public void encerrarInscricoes(Sorteio sorteio) {
        throw new IllegalStateException("sorteio já encerrado");
    }

    @Override
    public void executarSorteio(Sorteio sorteio) {
        throw new IllegalStateException("sorteio já encerrado");
    }

    @Override
    public void encerrar(Sorteio sorteio) {
        throw new IllegalStateException("sorteio já encerrado");
    }

    @Override
    public void cancelar(Sorteio sorteio) {
        throw new IllegalStateException("sorteio já encerrado — não pode ser cancelado");
    }

    @Override
    public StatusSorteio getStatus() {
        return StatusSorteio.ENCERRADO;
    }
}
