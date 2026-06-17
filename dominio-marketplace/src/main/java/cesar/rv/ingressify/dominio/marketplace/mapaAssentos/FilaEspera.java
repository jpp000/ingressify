package cesar.rv.ingressify.dominio.marketplace.mapaAssentos;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public class FilaEspera {

    private Integer id;
    private final MapaAssentosId mapaId;
    private final EventoId eventoId;
    private final UsuarioId usuarioId;
    private int posicao;
    private final LocalDateTime entradaEm;

    public FilaEspera(MapaAssentosId mapaId, EventoId eventoId, UsuarioId usuarioId, int posicao) {
        Validate.notNull(mapaId, "mapaId");
        Validate.notNull(eventoId, "eventoId");
        Validate.notNull(usuarioId, "usuarioId");
        Validate.isTrue(posicao >= 1, "posicao deve ser >= 1");
        this.mapaId = mapaId;
        this.eventoId = eventoId;
        this.usuarioId = usuarioId;
        this.posicao = posicao;
        this.entradaEm = LocalDateTime.now();
    }

    public FilaEspera(Integer id, MapaAssentosId mapaId, EventoId eventoId,
            UsuarioId usuarioId, int posicao, LocalDateTime entradaEm) {
        Validate.notNull(id, "id");
        Validate.notNull(mapaId, "mapaId");
        Validate.notNull(eventoId, "eventoId");
        Validate.notNull(usuarioId, "usuarioId");
        this.id = id;
        this.mapaId = mapaId;
        this.eventoId = eventoId;
        this.usuarioId = usuarioId;
        this.posicao = posicao;
        this.entradaEm = entradaEm;
    }

    public void atribuirId(Integer novoId) { this.id = novoId; }

    public Integer getId() { return id; }
    public MapaAssentosId getMapaId() { return mapaId; }
    public EventoId getEventoId() { return eventoId; }
    public UsuarioId getUsuarioId() { return usuarioId; }
    public int getPosicao() { return posicao; }
    public LocalDateTime getEntradaEm() { return entradaEm; }
}
