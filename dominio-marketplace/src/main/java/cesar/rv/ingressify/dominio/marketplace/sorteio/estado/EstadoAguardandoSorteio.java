package cesar.rv.ingressify.dominio.marketplace.sorteio.estado;

import cesar.rv.ingressify.dominio.marketplace.sorteio.Sorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusSorteio;

public class EstadoAguardandoSorteio implements EstadoSorteio {

    @Override
    public void abrirInscricoes(Sorteio sorteio) {
        throw new IllegalStateException("inscrições já foram encerradas");
    }

    @Override
    public void encerrarInscricoes(Sorteio sorteio) {
        throw new IllegalStateException("inscrições já foram encerradas");
    }

    @Override
    public void executarSorteio(Sorteio sorteio) {
        sorteio.aplicarStatus(StatusSorteio.SORTEADO);
    }

    @Override
    public void encerrar(Sorteio sorteio) {
        throw new IllegalStateException("sorteio ainda não foi executado");
    }

    @Override
    public void cancelar(Sorteio sorteio) {
        sorteio.aplicarStatus(StatusSorteio.CANCELADO);
    }

    @Override
    public StatusSorteio getStatus() {
        return StatusSorteio.AGUARDANDO_SORTEIO;
    }
}
