package cesar.rv.ingressify.dominio.marketplace.mapaAssentos;

import java.util.List;

import cesar.rv.ingressify.dominio.padroes.iterador.IIterador;

/**
 * Iterador concreto para a coleção de assentos de uma seção do mapa.
 */
public class AssentoIterador implements IIterador<Assento> {

    private final List<Assento> assentos;
    private int posicao = 0;

    public AssentoIterador(List<Assento> assentos) {
        this.assentos = assentos;
    }

    @Override
    public boolean temProximo() {
        return posicao < assentos.size();
    }

    @Override
    public Assento proximo() {
        if (!temProximo()) throw new java.util.NoSuchElementException("sem mais assentos");
        return assentos.get(posicao++);
    }
}
