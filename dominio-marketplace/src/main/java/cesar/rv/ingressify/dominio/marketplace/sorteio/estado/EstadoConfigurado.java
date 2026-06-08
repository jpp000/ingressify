package cesar.rv.ingressify.dominio.marketplace.sorteio.estado;

import cesar.rv.ingressify.dominio.marketplace.sorteio.Sorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusSorteio;

public class EstadoConfigurado implements EstadoSorteio {

    @Override
    public void abrirInscricoes(Sorteio sorteio) {
        sorteio.aplicarStatus(StatusSorteio.INSCRICOES_ABERTAS);
    }

    @Override
    public void encerrarInscricoes(Sorteio sorteio) {
        throw new IllegalStateException("inscrições ainda não foram abertas");
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
        return StatusSorteio.CONFIGURADO;
    }
}
