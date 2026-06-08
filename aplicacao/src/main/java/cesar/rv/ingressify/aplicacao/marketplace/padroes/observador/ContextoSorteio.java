package cesar.rv.ingressify.aplicacao.marketplace.padroes.observador;

import java.util.List;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioId;

public record ContextoSorteio(
        SorteioId sorteioId,
        EventoId eventoId,
        List<UsuarioId> contemplados,
        List<UsuarioId> listaEspera
) {}
