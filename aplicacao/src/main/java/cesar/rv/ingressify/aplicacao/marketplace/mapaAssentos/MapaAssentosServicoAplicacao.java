package cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.Assento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.AssentoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentos;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosServico;

public class MapaAssentosServicoAplicacao {

    private final MapaAssentosServico mapaServico;

    public MapaAssentosServicoAplicacao(MapaAssentosServico mapaServico) {
        Validate.notNull(mapaServico, "mapaServico");
        this.mapaServico = mapaServico;
    }

    public MapaAssentosId criarMapa(EventoId eventoId, int linhas, int colunas,
            BigDecimal precoNormal, BigDecimal precoVip, UsuarioId organizadorId) {
        MapaAssentos mapa = mapaServico.criarMapa(eventoId, linhas, colunas, precoNormal, precoVip);
        return mapa.getId();
    }

    public void reservarAssentos(List<AssentoId> assentoIds, UsuarioId usuarioId) {
        mapaServico.reservarAssentos(assentoIds, usuarioId);
    }

    public void confirmarVenda(List<AssentoId> assentoIds, UsuarioId usuarioId) {
        mapaServico.confirmarVenda(assentoIds, usuarioId);
    }

    public void liberarReservasExpiradas(MapaAssentosId mapaId) {
        mapaServico.liberarReservasExpiradas(mapaId);
    }

    public MapaAssentos obterPorEvento(EventoId eventoId) {
        return mapaServico.obterPorEvento(eventoId);
    }

    public List<Assento> listarAssentos(MapaAssentosId mapaId) {
        return mapaServico.listarAssentos(mapaId);
    }
}
