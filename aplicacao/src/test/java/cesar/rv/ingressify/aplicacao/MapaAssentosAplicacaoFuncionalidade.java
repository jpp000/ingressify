package cesar.rv.ingressify.aplicacao;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos.MapaAssentosServicoAplicacao;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoServico;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.Assento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.AssentoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentos;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosRepositorio;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosServico;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.StatusAssento;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class MapaAssentosAplicacaoFuncionalidade {

    private static final UsuarioId ORGANIZADOR = new UsuarioId(1);

    private final MapaAssentosRepositorioMemoria mapaRepositorio = new MapaAssentosRepositorioMemoria();
    private final MapaAssentosServico mapaServico = new MapaAssentosServico(mapaRepositorio);

    private final EventoRepositorioMemoria eventoRepositorio = new EventoRepositorioMemoria();
    private final EventoServico eventoServico = new EventoServico(eventoRepositorio);

    private final MapaAssentosServicoAplicacao servicoAplicacao =
            new MapaAssentosServicoAplicacao(mapaServico, eventoServico);

    private EventoId eventoIdParaMapa;
    private Throwable excecao;

    @Dado("um evento sem capacidade numerada configurada")
    public void eventoSemCapacidadeNumerada() {
        Evento e = new Evento(ORGANIZADOR, "Festa", LocalDateTime.now().plusDays(30),
                "Parque", "Descrição.", 200, 0, null, 7,
                LocalDateTime.now().plusDays(30).minusHours(2), "SHOW");
        eventoServico.salvar(e);
        eventoIdParaMapa = e.getId();
    }

    @Quando("o organizador tenta criar um mapa de assentos para este evento")
    public void organizadorTentaCriarMapaSemCapacidadeNumerada() {
        try {
            servicoAplicacao.criarMapa(eventoIdParaMapa, 3, 4,
                    new BigDecimal("80.00"), new BigDecimal("120.00"), ORGANIZADOR);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Então("a criação do mapa é rejeitada por falta de capacidade numerada")
    public void criacaoMapaRejeitadaPorFaltaCapacidadeNumerada() {
        assertNotNull(excecao);
        assertTrue(excecao.getMessage().contains("capacidade numerada"));
    }

    @Dado("um evento com capacidade total 100 e capacidade numerada 20")
    public void eventoComCapacidadeNumerada20() {
        Evento e = new Evento(ORGANIZADOR, "Conferência", LocalDateTime.now().plusDays(30),
                "Auditório", "Descrição.", 100, 20, null, 7,
                LocalDateTime.now().plusDays(30).minusHours(2), "SHOW");
        eventoServico.salvar(e);
        eventoIdParaMapa = e.getId();
    }

    @Quando("o organizador tenta criar um mapa com 5 fileiras e 5 assentos por fileira")
    public void organizadorTentaCriarMapa5x5() {
        try {
            servicoAplicacao.criarMapa(eventoIdParaMapa, 5, 5,
                    new BigDecimal("80.00"), new BigDecimal("120.00"), ORGANIZADOR);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Então("a criação do mapa é rejeitada por exceder capacidade numerada")
    public void criacaoMapaRejeitadaPorExcederCapacidadeNumerada() {
        assertNotNull(excecao);
        assertTrue(excecao.getMessage().contains("excede a capacidade numerada"));
    }

    // ── Repositórios em memória ───────────────────────────────────────────────

    static class EventoRepositorioMemoria implements EventoRepositorio {
        private final Map<EventoId, Evento> dados = new HashMap<>();
        private int proximoId = 1;

        @Override
        public void salvar(Evento evento) {
            if (evento.getId() == null) evento.atribuirId(new EventoId(proximoId++));
            dados.put(evento.getId(), evento);
        }

        @Override
        public Evento obter(EventoId id) {
            Evento e = dados.get(id);
            if (e == null) throw new IllegalArgumentException("Evento não encontrado: " + id);
            return e;
        }

        @Override
        public void remover(EventoId id) { dados.remove(id); }

        @Override
        public List<Evento> listarAtivos() {
            return dados.values().stream()
                    .filter(e -> e.getStatus() == StatusEvento.ATIVO).toList();
        }

        @Override
        public List<Evento> listarPorOrganizador(UsuarioId organizadorId) {
            return dados.values().stream()
                    .filter(e -> e.getOrganizadorId().equals(organizadorId)).toList();
        }

        @Override
        public List<Evento> pesquisarPorIds(Collection<EventoId> ids) {
            return ids.stream().map(dados::get).filter(e -> e != null).toList();
        }
    }

    static class MapaAssentosRepositorioMemoria implements MapaAssentosRepositorio {
        private final Map<MapaAssentosId, MapaAssentos> mapas = new HashMap<>();
        private final Map<AssentoId, Assento> assentos = new HashMap<>();
        private int proximoMapaId = 1;
        private int proximoAssentoId = 1;

        @Override
        public void salvar(MapaAssentos m) {
            if (m.getId() == null) m.atribuirId(new MapaAssentosId(proximoMapaId++));
            mapas.put(m.getId(), m);
        }

        @Override
        public MapaAssentos obter(MapaAssentosId id) {
            MapaAssentos m = mapas.get(id);
            if (m == null) throw new IllegalArgumentException("Mapa não encontrado: " + id);
            return m;
        }

        @Override
        public Optional<MapaAssentos> buscarPorEvento(EventoId eventoId) {
            return mapas.values().stream()
                    .filter(m -> m.getEventoId().equals(eventoId)).findFirst();
        }

        @Override
        public void salvarAssento(Assento a) {
            if (a.getId() == null) a.atribuirId(new AssentoId(proximoAssentoId++));
            assentos.put(a.getId(), a);
        }

        @Override
        public Assento obterAssento(AssentoId id) {
            Assento a = assentos.get(id);
            if (a == null) throw new IllegalArgumentException("Assento não encontrado: " + id);
            return a;
        }

        @Override
        public List<Assento> listarAssentosPorMapa(MapaAssentosId mapaId) {
            List<Assento> resultado = new ArrayList<>();
            for (Assento a : assentos.values()) {
                if (a.getMapaId().equals(mapaId)) resultado.add(a);
            }
            return resultado;
        }

        @Override
        public List<Assento> listarAssentosPorEvento(EventoId eventoId) {
            List<Assento> resultado = new ArrayList<>();
            for (Assento a : assentos.values()) {
                if (a.getEventoId().equals(eventoId)) resultado.add(a);
            }
            return resultado;
        }

        @Override
        public List<Assento> listarAssentosCompradosPor(UsuarioId usuarioId) {
            List<Assento> resultado = new ArrayList<>();
            for (Assento a : assentos.values()) {
                if (a.getStatus() == StatusAssento.VENDIDO
                        && usuarioId.equals(a.getReservadoPor())) {
                    resultado.add(a);
                }
            }
            return resultado;
        }

        @Override
        public List<MapaAssentosId> listarTodosIds() {
            return new ArrayList<>(mapas.keySet());
        }
    }
}
