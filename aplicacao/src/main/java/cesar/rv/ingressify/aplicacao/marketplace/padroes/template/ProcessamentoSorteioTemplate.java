package cesar.rv.ingressify.aplicacao.marketplace.padroes.template;

/**
 * Padrão Template Method: define o algoritmo fixo de execução do sorteio.
 * Subclasses implementam os passos variáveis (seleção e notificação).
 */
public abstract class ProcessamentoSorteioTemplate {

    public final void executar(ContextoSorteioProcessamento ctx) {
        validarPreCondicoes(ctx);
        selecionarContemplados(ctx);
        selecionarListaEspera(ctx);
        marcarInscricoes(ctx);
        atualizarSorteio(ctx);
        notificarContemplados(ctx);
    }

    protected abstract void validarPreCondicoes(ContextoSorteioProcessamento ctx);

    protected abstract void selecionarContemplados(ContextoSorteioProcessamento ctx);

    protected abstract void selecionarListaEspera(ContextoSorteioProcessamento ctx);

    protected abstract void marcarInscricoes(ContextoSorteioProcessamento ctx);

    protected abstract void atualizarSorteio(ContextoSorteioProcessamento ctx);

    protected abstract void notificarContemplados(ContextoSorteioProcessamento ctx);
}
