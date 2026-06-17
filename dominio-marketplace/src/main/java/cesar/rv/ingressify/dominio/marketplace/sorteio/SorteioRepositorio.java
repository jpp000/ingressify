package cesar.rv.ingressify.dominio.marketplace.sorteio;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface SorteioRepositorio {

    void salvar(Sorteio sorteio);

    Sorteio obter(SorteioId id);

    List<Sorteio> pesquisarPorEvento(EventoId eventoId);
}
