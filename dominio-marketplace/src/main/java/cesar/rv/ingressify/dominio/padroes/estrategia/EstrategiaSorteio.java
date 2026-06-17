package cesar.rv.ingressify.dominio.padroes.estrategia;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteio;

/**
 * Padrão Strategy: algoritmo de seleção dos contemplados em um sorteio.
 */
public interface EstrategiaSorteio {

    /**
     * Seleciona até {@code quantidade} inscritos da lista de elegíveis.
     * A ordem retornada define a posição (índice 0 = 1º contemplado).
     */
    List<InscricaoSorteio> selecionar(List<InscricaoSorteio> elegíveis, int quantidade);
}
