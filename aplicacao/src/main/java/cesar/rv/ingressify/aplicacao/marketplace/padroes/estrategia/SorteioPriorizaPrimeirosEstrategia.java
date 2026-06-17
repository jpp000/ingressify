package cesar.rv.ingressify.aplicacao.marketplace.padroes.estrategia;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteio;
import cesar.rv.ingressify.dominio.padroes.estrategia.EstrategiaSorteio;

/**
 * Estratégia que prioriza inscrições mais antigas, favorecendo quem se inscreveu primeiro.
 * Adequada para eventos que valorizam fidelidade e antecedência.
 */
public class SorteioPriorizaPrimeirosEstrategia implements EstrategiaSorteio {

    @Override
    public List<InscricaoSorteio> selecionar(List<InscricaoSorteio> elegiveis, int quantidade) {
        List<InscricaoSorteio> ordenados = new ArrayList<>(elegiveis);
        ordenados.sort(Comparator.comparing(InscricaoSorteio::getInscritoEm));
        return ordenados.subList(0, Math.min(quantidade, ordenados.size()));
    }
}
