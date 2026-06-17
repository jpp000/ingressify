package cesar.rv.ingressify.dominio.marketplace.mapaAssentos;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public class MapaAssentos {

    private MapaAssentosId id;
    private final EventoId eventoId;
    private final int totalLinhas;
    private final int totalColunas;

    public MapaAssentos(EventoId eventoId, int totalLinhas, int totalColunas) {
        Validate.notNull(eventoId, "eventoId");
        Validate.isTrue(totalLinhas > 0, "totalLinhas deve ser maior que zero");
        Validate.isTrue(totalColunas > 0, "totalColunas deve ser maior que zero");
        this.eventoId = eventoId;
        this.totalLinhas = totalLinhas;
        this.totalColunas = totalColunas;
    }

    public MapaAssentos(MapaAssentosId id, EventoId eventoId, int totalLinhas, int totalColunas) {
        Validate.notNull(id, "id");
        Validate.notNull(eventoId, "eventoId");
        this.id = id;
        this.eventoId = eventoId;
        this.totalLinhas = totalLinhas;
        this.totalColunas = totalColunas;
    }

    public void atribuirId(MapaAssentosId novoId) {
        Validate.notNull(novoId, "novoId");
        this.id = novoId;
    }

    public MapaAssentosId getId() { return id; }
    public EventoId getEventoId() { return eventoId; }
    public int getTotalLinhas() { return totalLinhas; }
    public int getTotalColunas() { return totalColunas; }
}
