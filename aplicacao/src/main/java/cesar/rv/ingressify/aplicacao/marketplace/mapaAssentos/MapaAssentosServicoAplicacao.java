package cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoServico;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.Assento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.AssentoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentos;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosServico;

public class MapaAssentosServicoAplicacao {

    private final MapaAssentosServico mapaServico;
    private final EventoServico eventoServico;

    public MapaAssentosServicoAplicacao(MapaAssentosServico mapaServico, EventoServico eventoServico) {
        Validate.notNull(mapaServico, "mapaServico");
        Validate.notNull(eventoServico, "eventoServico");
        this.mapaServico = mapaServico;
        this.eventoServico = eventoServico;
    }

    public MapaAssentosId criarMapa(EventoId eventoId, int linhas, int colunas,
            BigDecimal precoNormal, BigDecimal precoVip, UsuarioId organizadorId) {
        Evento evento = eventoServico.obter(eventoId);

        if (!evento.getOrganizadorId().equals(organizadorId)) {
            throw new IllegalStateException("somente o organizador pode criar mapa de assentos");
        }
        if (!evento.temAssentosNumerados()) {
            throw new IllegalStateException(
                    "evento não possui capacidade numerada — configure assentos numerados ao criar o evento");
        }
        int totalAssentos = linhas * colunas;
        if (totalAssentos > evento.getCapacidadeNumerada()) {
            throw new IllegalStateException(
                    "mapa de " + totalAssentos + " assentos excede a capacidade numerada do evento ("
                    + evento.getCapacidadeNumerada() + ")");
        }

        MapaAssentos mapa = mapaServico.criarMapa(eventoId, linhas, colunas, precoNormal, precoVip);
        return mapa.getId();
    }

    public void bloquearAssentos(MapaAssentosId mapaId, List<AssentoId> assentoIds, UsuarioId organizadorId) {
        MapaAssentos mapa = mapaServico.obterPorId(mapaId);
        Evento evento = eventoServico.obter(mapa.getEventoId());
        if (!evento.getOrganizadorId().equals(organizadorId)) {
            throw new IllegalStateException("somente o organizador pode bloquear assentos");
        }
        mapaServico.bloquearAssentos(assentoIds);
    }

    public void desbloquearAssentos(MapaAssentosId mapaId, List<AssentoId> assentoIds, UsuarioId organizadorId) {
        MapaAssentos mapa = mapaServico.obterPorId(mapaId);
        Evento evento = eventoServico.obter(mapa.getEventoId());
        if (!evento.getOrganizadorId().equals(organizadorId)) {
            throw new IllegalStateException("somente o organizador pode desbloquear assentos");
        }
        mapaServico.desbloquearAssentos(assentoIds);
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

    public List<Assento> listarPorComprador(UsuarioId usuarioId) {
        return mapaServico.listarPorComprador(usuarioId);
    }
}
