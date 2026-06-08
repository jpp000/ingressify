package cesar.rv.ingressify.aplicacao.marketplace.padroes.estrategia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteio;
import cesar.rv.ingressify.dominio.padroes.estrategia.EstrategiaSorteio;

/**
 * Estratégia de sorteio puramente aleatório (shuffle da lista de inscritos).
 */
public class SorteioAleatorioEstrategia implements EstrategiaSorteio {

    @Override
    public List<InscricaoSorteio> selecionar(List<InscricaoSorteio> elegiveis, int quantidade) {
        List<InscricaoSorteio> embaralhados = new ArrayList<>(elegiveis);
        Collections.shuffle(embaralhados);
        return embaralhados.subList(0, Math.min(quantidade, embaralhados.size()));
    }
}
