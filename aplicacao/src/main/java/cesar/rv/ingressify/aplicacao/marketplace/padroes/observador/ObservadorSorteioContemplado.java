package cesar.rv.ingressify.aplicacao.marketplace.padroes.observador;

import cesar.rv.ingressify.dominio.padroes.observador.Observador;

/**
 * Observador que registra o resultado do sorteio (contemplados e lista de espera).
 * Em produção conectaria a um serviço de notificação (e-mail, push).
 */
public class ObservadorSorteioContemplado implements Observador<ContextoSorteio> {

    @Override
    public void notificar(ContextoSorteio contexto) {
        // Ponto de extensão: envio de e-mail/notificação push para contemplados
        // Por ora registra intenção — integração real via serviço externo
        contexto.contemplados().forEach(u ->
            System.out.printf("[SORTEIO] Usuário %d contemplado no sorteio %d do evento %d%n",
                u.getId(), contexto.sorteioId().getId(), contexto.eventoId().getId()));
        contexto.listaEspera().forEach(u ->
            System.out.printf("[SORTEIO] Usuário %d em lista de espera no sorteio %d%n",
                u.getId(), contexto.sorteioId().getId()));
    }
}
