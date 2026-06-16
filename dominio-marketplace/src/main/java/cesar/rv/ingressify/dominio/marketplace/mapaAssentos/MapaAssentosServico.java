package cesar.rv.ingressify.dominio.marketplace.mapaAssentos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.padroes.decorador.AssentoAcessibilidadeDecorador;
import cesar.rv.ingressify.dominio.padroes.decorador.AssentoBase;
import cesar.rv.ingressify.dominio.padroes.decorador.AssentoComponente;
import cesar.rv.ingressify.dominio.padroes.decorador.AssentoVipDecorador;
import cesar.rv.ingressify.dominio.padroes.iterador.IIterador;

public class MapaAssentosServico {

    private final MapaAssentosRepositorio repositorio;
    private final AssentoReservaProxy reservaProxy;

    public MapaAssentosServico(MapaAssentosRepositorio repositorio) {
        Validate.notNull(repositorio, "repositorio");
        this.repositorio = repositorio;
        this.reservaProxy = new AssentoReservaProxy(repositorio);
    }

    public MapaAssentos criarMapa(EventoId eventoId, int linhas, int colunas,
            BigDecimal precoNormal, BigDecimal precoVip) {
        repositorio.buscarPorEvento(eventoId).ifPresent(m -> {
            throw new IllegalStateException("já existe mapa de assentos para este evento");
        });

        MapaAssentos mapa = new MapaAssentos(eventoId, linhas, colunas);
        repositorio.salvar(mapa);

        gerarAssentos(mapa, precoNormal, precoVip);
        return mapa;
    }

    /**
     * Usa o padrão Decorator para calcular o preço de cada assento.
     * Usa o padrão Iterator para percorrer a grade de assentos criados.
     */
    private void gerarAssentos(MapaAssentos mapa, BigDecimal precoNormal, BigDecimal precoVip) {
        List<Assento> assentos = new ArrayList<>();
        char[] letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        for (int linha = 0; linha < mapa.getTotalLinhas(); linha++) {
            char letra = letras[linha % letras.length];
            for (int col = 1; col <= mapa.getTotalColunas(); col++) {
                String codigo = letra + String.valueOf(col);
                TipoAssento tipo = determinarTipo(linha, col, mapa.getTotalLinhas(), mapa.getTotalColunas());

                // Aplica Decorator para calcular preço conforme tipo
                AssentoComponente componente = new AssentoBase(codigo, precoNormal, TipoAssento.NORMAL);
                AssentoComponente decorado = switch (tipo) {
                    case VIP -> new AssentoVipDecorador(componente);
                    case ACESSIBILIDADE -> new AssentoAcessibilidadeDecorador(componente);
                    default -> componente;
                };

                Assento assento = new Assento(mapa.getId(), mapa.getEventoId(),
                        "Principal", codigo, decorado.getTipo(), decorado.getPreco());
                assentos.add(assento);
            }
        }

        // Usa Iterator para persistir assentos
        ColecaoAssentos colecao = new ColecaoAssentos(assentos);
        IIterador<Assento> iterador = colecao.criarIterador();
        while (iterador.temProximo()) {
            repositorio.salvarAssento(iterador.proximo());
        }
    }

    private TipoAssento determinarTipo(int linha, int col, int totalLinhas, int totalColunas) {
        boolean primeiraFileira = linha == 0;
        boolean colunaCentral = col == totalColunas / 2 || col == totalColunas / 2 + 1;
        boolean ultimaFileira = linha == totalLinhas - 1;
        boolean primeiraOuUltimaColuna = col == 1 || col == totalColunas;

        if (primeiraFileira && colunaCentral) return TipoAssento.VIP;
        if (ultimaFileira && primeiraOuUltimaColuna) return TipoAssento.ACESSIBILIDADE;
        return TipoAssento.NORMAL;
    }

    public void reservarAssentos(List<AssentoId> assentoIds, UsuarioId usuarioId) {
        reservaProxy.reservar(assentoIds, usuarioId);
    }

    public void confirmarVenda(List<AssentoId> assentoIds, UsuarioId usuarioId) {
        for (AssentoId assentoId : assentoIds) {
            Assento assento = repositorio.obterAssento(assentoId);
            if (assento.getReservadoPor() == null || !assento.getReservadoPor().equals(usuarioId)) {
                throw new IllegalStateException("assento " + assento.getCodigo()
                        + " não está reservado pelo usuário informado");
            }
            assento.confirmarVenda();
            repositorio.salvarAssento(assento);
        }
    }

    public void liberarReservasExpiradas(MapaAssentosId mapaId) {
        List<Assento> todos = repositorio.listarAssentosPorMapa(mapaId);
        ColecaoAssentos colecao = new ColecaoAssentos(todos);
        IIterador<Assento> iterador = colecao.criarIterador();
        while (iterador.temProximo()) {
            Assento a = iterador.proximo();
            if (a.reservaExpirada()) {
                a.liberarReserva();
                repositorio.salvarAssento(a);
            }
        }
    }

    public MapaAssentos obterPorId(MapaAssentosId mapaId) {
        return repositorio.obter(mapaId);
    }

    public MapaAssentos obterPorEvento(EventoId eventoId) {
        return repositorio.buscarPorEvento(eventoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mapa de assentos não encontrado para o evento: " + eventoId));
    }

    public List<Assento> listarAssentos(MapaAssentosId mapaId) {
        return repositorio.listarAssentosPorMapa(mapaId);
    }

    public void bloquearAssentos(List<AssentoId> assentoIds) {
        for (AssentoId id : assentoIds) {
            Assento a = repositorio.obterAssento(id);
            a.bloquear();
            repositorio.salvarAssento(a);
        }
    }

    public void desbloquearAssentos(List<AssentoId> assentoIds) {
        for (AssentoId id : assentoIds) {
            Assento a = repositorio.obterAssento(id);
            a.desbloquear();
            repositorio.salvarAssento(a);
        }
    }

    public List<MapaAssentosId> listarTodosIds() {
        return repositorio.listarTodosIds();
    }

    public Assento obterAssento(AssentoId assentoId) {
        return repositorio.obterAssento(assentoId);
    }
}
