package cesar.rv.ingressify.aplicacao.marketplace.padroes.observador;

import cesar.rv.ingressify.dominio.padroes.observador.PublicadorBase;

/**
 * Publicador de eventos de sorteio. Estende PublicadorBase para reutilizar
 * a infraestrutura do padrão Observer já presente no sistema.
 */
public class PublicadorSorteio extends PublicadorBase<ContextoSorteio> {

    public void publicarResultado(ContextoSorteio contexto) {
        notificarTodos(contexto);
    }
}
