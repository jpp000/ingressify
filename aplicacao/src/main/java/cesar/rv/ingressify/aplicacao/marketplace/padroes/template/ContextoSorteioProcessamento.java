package cesar.rv.ingressify.aplicacao.marketplace.padroes.template;

import java.util.ArrayList;
import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.Sorteio;

public class ContextoSorteioProcessamento {

    private final Sorteio sorteio;
    private final List<InscricaoSorteio> inscricoes;
    private List<InscricaoSorteio> contemplados = new ArrayList<>();
    private List<InscricaoSorteio> listaEspera = new ArrayList<>();

    public ContextoSorteioProcessamento(Sorteio sorteio, List<InscricaoSorteio> inscricoes) {
        this.sorteio = sorteio;
        this.inscricoes = inscricoes;
    }

    public Sorteio getSorteio() { return sorteio; }
    public List<InscricaoSorteio> getInscricoes() { return inscricoes; }
    public List<InscricaoSorteio> getContemplados() { return contemplados; }
    public List<InscricaoSorteio> getListaEspera() { return listaEspera; }
    public void setContemplados(List<InscricaoSorteio> contemplados) { this.contemplados = contemplados; }
    public void setListaEspera(List<InscricaoSorteio> listaEspera) { this.listaEspera = listaEspera; }
}
