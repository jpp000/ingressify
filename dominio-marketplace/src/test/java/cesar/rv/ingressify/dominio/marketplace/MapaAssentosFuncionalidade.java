package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.Assento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.AssentoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentos;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosRepositorio;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosServico;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.StatusAssento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.TipoAssento;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class MapaAssentosFuncionalidade {

    private static final UsuarioId ORGANIZADOR = new UsuarioId(1);
    private static final UsuarioId COMPRADOR_A = new UsuarioId(10);
    private static final UsuarioId COMPRADOR_B = new UsuarioId(11);

    private final MapaAssentosRepositorioMemoria repositorio = new MapaAssentosRepositorioMemoria();
    private final MapaAssentosServico servico = new MapaAssentosServico(repositorio);

    private MapaAssentos mapa;
    private List<Assento> assentos;
    private Throwable excecao;

    // ---- criar mapa ----

    @Dado("um evento sem mapa de assentos configurado")
    public void eventoSemMapa() { }

    @Quando("o organizador cria um mapa com 3 fileiras e 4 assentos por fileira")
    public void criarMapa3x4() {
        mapa = servico.criarMapa(new EventoId(200), 3, 4,
                new BigDecimal("100.00"), new BigDecimal("150.00"));
    }

    @Então("o mapa é criado com 12 assentos")
    public void mapaCom12Assentos() {
        assertNotNull(mapa);
        assertEquals(12, repositorio.listarAssentosPorMapa(mapa.getId()).size());
    }

    // ---- assentos VIP ----

    @Dado("um mapa de assentos com 3 fileiras e 6 colunas criado")
    public void criarMapa3x6() {
        mapa = servico.criarMapa(new EventoId(201), 3, 6,
                new BigDecimal("100.00"), new BigDecimal("150.00"));
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
    }

    @Quando("o organizador lista os assentos do mapa")
    public void listarAssentos() {
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
    }

    @Então("assentos na fileira A colunas 3 e 4 são do tipo VIP")
    public void assentosVipFileira() {
        long vip = assentos.stream()
                .filter(a -> (a.getCodigo().equals("A3") || a.getCodigo().equals("A4"))
                        && a.getTipo() == TipoAssento.VIP)
                .count();
        assertEquals(2, vip);
    }

    @Então("assentos na última fileira colunas 1 e 6 são do tipo ACESSIBILIDADE")
    public void assentosAcessibilidadeUltimaFileira() {
        long acessivel = assentos.stream()
                .filter(a -> (a.getCodigo().equals("C1") || a.getCodigo().equals("C6"))
                        && a.getTipo() == TipoAssento.ACESSIBILIDADE)
                .count();
        assertEquals(2, acessivel);
    }

    // ---- reservar assentos ----

    @Dado("um mapa de assentos criado com assentos disponíveis")
    public void mapaComAssentosDisponiveis() {
        mapa = servico.criarMapa(new EventoId(202), 2, 4,
                new BigDecimal("80.00"), new BigDecimal("120.00"));
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
    }

    @Quando("um comprador reserva 2 assentos")
    public void compradorReserva2Assentos() {
        List<AssentoId> ids = assentos.stream()
                .filter(a -> a.getStatus() == StatusAssento.DISPONIVEL
                        && a.getTipo() == TipoAssento.NORMAL)
                .sorted((a, b) -> a.getCodigo().compareTo(b.getCodigo()))
                .limit(2).map(Assento::getId).toList();
        servico.reservarAssentos(ids, COMPRADOR_A);
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
    }

    @Então("os 2 assentos ficam com status RESERVADO")
    public void assentosReservados() {
        long reservados = assentos.stream()
                .filter(a -> a.getStatus() == StatusAssento.RESERVADO).count();
        assertEquals(2, reservados);
    }

    // ---- assento já reservado ----

    @Dado("um mapa de assentos com um assento já reservado")
    public void mapaComAssentoReservado() {
        mapa = servico.criarMapa(new EventoId(203), 2, 4,
                new BigDecimal("80.00"), new BigDecimal("120.00"));
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
        Assento alvo = assentos.stream()
                .filter(a -> a.getStatus() == StatusAssento.DISPONIVEL
                        && a.getTipo() == TipoAssento.NORMAL)
                .findFirst().orElseThrow();
        servico.reservarAssentos(List.of(alvo.getId()), COMPRADOR_A);
    }

    @Quando("outro comprador tenta reservar o mesmo assento")
    public void outroComepradorReservaOMesmo() {
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
        Assento reservado = assentos.stream()
                .filter(a -> a.getStatus() == StatusAssento.RESERVADO)
                .findFirst().orElseThrow();
        try {
            servico.reservarAssentos(List.of(reservado.getId()), COMPRADOR_B);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Então("a reserva é rejeitada por indisponibilidade")
    public void reservaRejeitadaIndisponibilidade() {
        assertNotNull(excecao);
        assertTrue(excecao.getMessage().contains("não está disponível"));
    }

    // ---- anti-ilha ----

    @Dado("um mapa com 1 fileira e 4 assentos onde A4 está vendido")
    public void mapaComAssentoA4Vendido() {
        mapa = servico.criarMapa(new EventoId(204), 1, 4,
                new BigDecimal("80.00"), new BigDecimal("120.00"));
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
        Assento a4 = assentos.stream().filter(a -> a.getCodigo().equals("A4")).findFirst().orElseThrow();
        a4.reservar(COMPRADOR_B, 5);
        a4.confirmarVenda();
        repositorio.salvarAssento(a4);
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
    }

    @Quando("um comprador tenta reservar A1 e A3 ao mesmo tempo")
    public void compradorReservaA1eA3() {
        Assento a1 = assentos.stream().filter(a -> a.getCodigo().equals("A1")).findFirst().orElseThrow();
        Assento a3 = assentos.stream().filter(a -> a.getCodigo().equals("A3")).findFirst().orElseThrow();
        try {
            servico.reservarAssentos(List.of(a1.getId(), a3.getId()), COMPRADOR_A);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Então("a reserva é rejeitada por regra anti-ilha")
    public void reservaRejeitadaAntiIlha() {
        assertNotNull(excecao);
        assertTrue(excecao.getMessage().contains("isolaria"));
    }

    // ---- confirmar venda ----

    @Dado("um mapa de assentos com um assento reservado pelo comprador")
    public void mapaComAssentoReservadoPeloComprador() {
        mapa = servico.criarMapa(new EventoId(205), 2, 4,
                new BigDecimal("80.00"), new BigDecimal("120.00"));
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
        Assento alvo = assentos.stream()
                .filter(a -> a.getStatus() == StatusAssento.DISPONIVEL
                        && a.getTipo() == TipoAssento.NORMAL)
                .findFirst().orElseThrow();
        servico.reservarAssentos(List.of(alvo.getId()), COMPRADOR_A);
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
    }

    @Quando("o comprador confirma a compra do assento reservado")
    public void compradorConfirmaCompra() {
        Assento reservado = assentos.stream()
                .filter(a -> a.getStatus() == StatusAssento.RESERVADO)
                .findFirst().orElseThrow();
        servico.confirmarVenda(List.of(reservado.getId()), COMPRADOR_A);
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
    }

    @Então("o assento fica com status VENDIDO")
    public void assentoVendido() {
        long vendidos = assentos.stream()
                .filter(a -> a.getStatus() == StatusAssento.VENDIDO).count();
        assertEquals(1, vendidos);
    }

    // ---- liberar reservas expiradas ----

    @Dado("um mapa de assentos com uma reserva expirada")
    public void mapaComReservaExpirada() {
        mapa = servico.criarMapa(new EventoId(206), 1, 2,
                new BigDecimal("50.00"), new BigDecimal("75.00"));
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
        Assento a = assentos.stream()
                .filter(x -> x.getStatus() == StatusAssento.DISPONIVEL).findFirst().orElseThrow();
        a.reservar(COMPRADOR_A, 0);
        repositorio.salvarAssento(a);
    }

    @Quando("o sistema libera reservas expiradas")
    public void sistemaLiberaExpiradas() {
        servico.liberarReservasExpiradas(mapa.getId());
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
    }

    @Então("o assento volta ao status DISPONIVEL")
    public void assentoDisponivelNovamente() {
        assertTrue(assentos.stream().anyMatch(a -> a.getStatus() == StatusAssento.DISPONIVEL));
    }

    // ---- duplicidade ----

    @Dado("um evento com mapa de assentos já configurado")
    public void eventoComMapaExistente() {
        mapa = servico.criarMapa(new EventoId(207), 2, 4,
                new BigDecimal("80.00"), new BigDecimal("120.00"));
    }

    @Quando("o organizador tenta criar um segundo mapa para o mesmo evento")
    public void criarSegundoMapa() {
        try {
            servico.criarMapa(new EventoId(207), 2, 4,
                    new BigDecimal("80.00"), new BigDecimal("120.00"));
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Então("a criação é rejeitada por duplicidade")
    public void criacaoRejeitadaDuplicidade() {
        assertNotNull(excecao);
        assertTrue(excecao.getMessage().contains("já existe mapa"));
    }

    // ---- limite de assentos por usuário ----

    @Dado("um mapa com 2 fileiras e 6 assentos por fileira com 5 assentos já comprados pelo comprador")
    public void mapaComCincoAssentosCompradosPeloComprador() {
        mapa = servico.criarMapa(new EventoId(208), 2, 6,
                new BigDecimal("80.00"), new BigDecimal("120.00"));
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());

        List<Assento> normais = assentos.stream()
                .filter(a -> a.getStatus() == StatusAssento.DISPONIVEL && a.getTipo() == TipoAssento.NORMAL)
                .sorted((a, b) -> a.getCodigo().compareTo(b.getCodigo()))
                .toList();

        // Compra 5 assentos individualmente (anti-ilha com compras separadas)
        for (int i = 0; i < 5; i++) {
            Assento a = normais.get(i);
            a.reservar(COMPRADOR_A, 5);
            a.confirmarVenda();
            repositorio.salvarAssento(a);
        }
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
    }

    @Quando("o comprador tenta reservar 2 assentos adicionais")
    public void compradorTentaReservar2AssentosAdicionais() {
        List<AssentoId> disponiveis = assentos.stream()
                .filter(a -> a.getStatus() == StatusAssento.DISPONIVEL)
                .sorted((a, b) -> a.getCodigo().compareTo(b.getCodigo()))
                .limit(2)
                .map(Assento::getId)
                .toList();
        try {
            servico.reservarAssentos(disponiveis, COMPRADOR_A);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Então("a reserva é rejeitada por limite de assentos por usuário")
    public void reservaRejeitadaPorLimite() {
        assertNotNull(excecao);
        assertTrue(excecao.getMessage().contains("limite de"));
    }

    // ---- confirmação com reserva expirada ----

    @Dado("um mapa com um assento com reserva expirada pertencente ao comprador")
    public void mapaComAssentoReservadoExpirado() {
        mapa = servico.criarMapa(new EventoId(209), 1, 4,
                new BigDecimal("80.00"), new BigDecimal("120.00"));
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
        Assento alvo = assentos.stream()
                .filter(a -> a.getStatus() == StatusAssento.DISPONIVEL)
                .findFirst().orElseThrow();
        alvo.reservar(COMPRADOR_A, 0); // reserva com 0 minutos — já expirada
        repositorio.salvarAssento(alvo);
        assentos = repositorio.listarAssentosPorMapa(mapa.getId());
    }

    @Quando("o comprador tenta confirmar a venda do assento expirado")
    public void compradorConfirmaAssentoExpirado() {
        Assento reservado = assentos.stream()
                .filter(a -> a.getStatus() == StatusAssento.RESERVADO)
                .findFirst().orElseThrow();
        try {
            servico.confirmarVenda(List.of(reservado.getId()), COMPRADOR_A);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Então("a confirmação é rejeitada por reserva expirada")
    public void confirmacaoRejeitadaPorExpiracao() {
        assertNotNull(excecao);
        assertTrue(excecao.getMessage().contains("expirou"));
    }

    // ── Repositório em memória (apenas para testes) ───────────────────────────

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
        public List<MapaAssentosId> listarTodosIds() {
            return new ArrayList<>(mapas.keySet());
        }
    }
}
