package cesar.rv.ingressify.dominio.marketplace.mapaAssentos;

import java.util.List;

import cesar.rv.ingressify.dominio.padroes.iterador.IIterador;

/**
 * Padrão Iterator: coleção de assentos percorrível sem expor a estrutura interna.
 */
public class ColecaoAssentos {

    private final List<Assento> assentos;

    public ColecaoAssentos(List<Assento> assentos) {
        this.assentos = List.copyOf(assentos);
    }

    public IIterador<Assento> criarIterador() {
        return new AssentoIterador(assentos);
    }

    public int tamanho() {
        return assentos.size();
    }
}
