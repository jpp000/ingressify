package cesar.rv.ingressify.aplicacao.marketplace.padroes.template;

import java.util.List;

import cesar.rv.ingressify.aplicacao.marketplace.padroes.observador.ContextoSorteio;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.observador.PublicadorSorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusInscricao;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusSorteio;
import cesar.rv.ingressify.dominio.padroes.estrategia.EstrategiaSorteio;

/**
 * Implementação concreta do Template Method para execução de sorteios.
 * Usa a Strategy injetada para determinar os contemplados.
 */
public class ExecutarSorteioProcessamento extends ProcessamentoSorteioTemplate {

    private final EstrategiaSorteio estrategia;
    private final InscricaoSorteioRepositorio inscricaoRepositorio;
    private final PublicadorSorteio publicadorSorteio;

    public ExecutarSorteioProcessamento(EstrategiaSorteio estrategia,
            InscricaoSorteioRepositorio inscricaoRepositorio,
            PublicadorSorteio publicadorSorteio) {
        this.estrategia = estrategia;
        this.inscricaoRepositorio = inscricaoRepositorio;
        this.publicadorSorteio = publicadorSorteio;
    }

    @Override
    protected void validarPreCondicoes(ContextoSorteioProcessamento ctx) {
        if (ctx.getSorteio().getStatus() != StatusSorteio.AGUARDANDO_SORTEIO) {
            throw new IllegalStateException("sorteio não está aguardando execução");
        }
        if (ctx.getInscricoes().isEmpty()) {
            throw new IllegalStateException("não há inscrições para sortear");
        }
    }

    @Override
    protected void selecionarContemplados(ContextoSorteioProcessamento ctx) {
        List<InscricaoSorteio> elegiveis = ctx.getInscricoes().stream()
                .filter(i -> i.getStatus() == StatusInscricao.INSCRITO)
                .toList();
        List<InscricaoSorteio> selecionados = estrategia.selecionar(
                elegiveis, ctx.getSorteio().getQuantidadeIngressos());
        ctx.setContemplados(selecionados);
    }

    @Override
    protected void selecionarListaEspera(ContextoSorteioProcessamento ctx) {
        List<InscricaoSorteio> naoContemplados = ctx.getInscricoes().stream()
                .filter(i -> i.getStatus() == StatusInscricao.INSCRITO)
                .filter(i -> !ctx.getContemplados().contains(i))
                .toList();
        List<InscricaoSorteio> reservas = estrategia.selecionar(
                naoContemplados, ctx.getSorteio().getQuantidadeListaEspera());
        ctx.setListaEspera(reservas);
    }

    @Override
    protected void marcarInscricoes(ContextoSorteioProcessamento ctx) {
        for (int i = 0; i < ctx.getContemplados().size(); i++) {
            InscricaoSorteio inscricao = ctx.getContemplados().get(i);
            inscricao.contemplar(i + 1);
            inscricaoRepositorio.salvar(inscricao);
        }
        for (int i = 0; i < ctx.getListaEspera().size(); i++) {
            InscricaoSorteio inscricao = ctx.getListaEspera().get(i);
            inscricao.marcarListaEspera(i + 1);
            inscricaoRepositorio.salvar(inscricao);
        }
    }

    @Override
    protected void atualizarSorteio(ContextoSorteioProcessamento ctx) {
        ctx.getSorteio().executarSorteio();
    }

    @Override
    protected void notificarContemplados(ContextoSorteioProcessamento ctx) {
        ContextoSorteio contexto = new ContextoSorteio(
                ctx.getSorteio().getId(),
                ctx.getSorteio().getEventoId(),
                ctx.getContemplados().stream().map(InscricaoSorteio::getParticipanteId).toList(),
                ctx.getListaEspera().stream().map(InscricaoSorteio::getParticipanteId).toList()
        );
        publicadorSorteio.publicarResultado(contexto);
    }
}
