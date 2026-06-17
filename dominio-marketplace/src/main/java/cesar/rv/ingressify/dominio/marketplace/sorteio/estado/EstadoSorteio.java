package cesar.rv.ingressify.dominio.marketplace.sorteio.estado;

import cesar.rv.ingressify.dominio.marketplace.sorteio.Sorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusSorteio;

/**
 * Padrão State: encapsula as transições de estado do ciclo de vida de um Sorteio.
 * Cada estado concreto implementa apenas as transições válidas a partir dele.
 */
public interface EstadoSorteio {

    void abrirInscricoes(Sorteio sorteio);

    void encerrarInscricoes(Sorteio sorteio);

    void executarSorteio(Sorteio sorteio);

    void encerrar(Sorteio sorteio);

    void cancelar(Sorteio sorteio);

    StatusSorteio getStatus();
}
