package cesar.rv.ingressify.dominio.marketplace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaServico;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;
import cesar.rv.ingressify.dominio.marketplace.compra.Pedido;
import cesar.rv.ingressify.dominio.marketplace.compra.PedidoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoServico;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class MarketplaceFuncionalidade {

    protected final EventoServico eventoServico;
    protected final IngressoServico ingressoServico;
    protected final TipoIngressoServico tipoIngressoServico;
    protected final AnuncioRevendaServico anuncioRevendaServico;
    protected Throwable excecao;

    public MarketplaceFuncionalidade() {
        EventoRepositorioMemoria eventoRepo = new EventoRepositorioMemoria();
        IngressoRepositorioMemoria ingressoRepo = new IngressoRepositorioMemoria();
        TipoIngressoRepositorioMemoria tipoRepo = new TipoIngressoRepositorioMemoria();
        AnuncioRevendaRepositorioMemoria anuncioRepo = new AnuncioRevendaRepositorioMemoria();

        eventoServico = new EventoServico(eventoRepo);
        ingressoServico = new IngressoServico(ingressoRepo);
        tipoIngressoServico = new TipoIngressoServico(tipoRepo, eventoRepo);
        anuncioRevendaServico = new AnuncioRevendaServico(anuncioRepo, ingressoRepo, tipoRepo);
    }

    static class EventoRepositorioMemoria implements EventoRepositorio {
        private final Map<EventoId, Evento> dados = new HashMap<>();
        private int proximoId = 1;

        @Override
        public void salvar(Evento evento) {
            if (evento.getId() == null) {
                evento.atribuirId(new EventoId(proximoId++));
            }
            dados.put(evento.getId(), evento);
        }

        @Override
        public Evento obter(EventoId id) {
            Evento e = dados.get(id);
            if (e == null) {
                throw new IllegalArgumentException("Evento não encontrado: " + id);
            }
            return e;
        }

        @Override
        public void remover(EventoId id) {
            dados.remove(id);
        }
    }

    static class IngressoRepositorioMemoria implements IngressoRepositorio {
        private final Map<IngressoId, Ingresso> dados = new HashMap<>();

        @Override
        public void salvar(Ingresso ingresso) {
            if (ingresso.getId() == null) {
                ingresso.atribuirId(new IngressoId(UUID.randomUUID()));
            }
            dados.put(ingresso.getId(), ingresso);
        }

        @Override
        public Ingresso obter(IngressoId id) {
            Ingresso i = dados.get(id);
            if (i == null) {
                throw new IllegalArgumentException("Ingresso não encontrado: " + id);
            }
            return i;
        }

        @Override
        public List<Ingresso> pesquisarPorProprietario(UsuarioId proprietario) {
            List<Ingresso> resultado = new ArrayList<>();
            for (Ingresso i : dados.values()) {
                if (i.getProprietario().equals(proprietario)) {
                    resultado.add(i);
                }
            }
            return resultado;
        }
    }

    static class TipoIngressoRepositorioMemoria implements TipoIngressoRepositorio {
        private final Map<TipoIngressoId, TipoIngresso> dados = new HashMap<>();
        private int proximoId = 1;

        @Override
        public void salvar(TipoIngresso tipo) {
            if (tipo.getId() == null) {
                tipo.atribuirId(new TipoIngressoId(proximoId++));
            }
            dados.put(tipo.getId(), tipo);
        }

        @Override
        public TipoIngresso obter(TipoIngressoId id) {
            TipoIngresso t = dados.get(id);
            if (t == null) {
                throw new IllegalArgumentException("Tipo de ingresso não encontrado: " + id);
            }
            return t;
        }

        @Override
        public List<TipoIngresso> pesquisarPorEvento(EventoId eventoId) {
            List<TipoIngresso> resultado = new ArrayList<>();
            for (TipoIngresso t : dados.values()) {
                if (t.getEventoId().equals(eventoId)) {
                    resultado.add(t);
                }
            }
            return resultado;
        }
    }

    static class AnuncioRevendaRepositorioMemoria implements AnuncioRevendaRepositorio {
        private final Map<AnuncioRevendaId, AnuncioRevenda> dados = new HashMap<>();
        private int proximoId = 1;

        @Override
        public void salvar(AnuncioRevenda anuncio) {
            if (anuncio.getId() == null) {
                anuncio.atribuirId(new AnuncioRevendaId(proximoId++));
            }
            dados.put(anuncio.getId(), anuncio);
        }

        @Override
        public AnuncioRevenda obter(AnuncioRevendaId id) {
            AnuncioRevenda a = dados.get(id);
            if (a == null) {
                throw new IllegalArgumentException("Anúncio não encontrado: " + id);
            }
            return a;
        }

        @Override
        public void remover(AnuncioRevendaId id) {
            dados.remove(id);
        }

        @Override
        public boolean existeDisponivelOuReservadoParaIngresso(IngressoId ingressoId) {
            for (AnuncioRevenda a : dados.values()) {
                if (a.getStatus() == StatusAnuncio.DISPONIVEL || a.getStatus() == StatusAnuncio.RESERVADO) {
                    for (IngressoId iid : a.getIngressoIds()) {
                        if (iid.equals(ingressoId)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    static class PedidoRepositorioMemoria implements PedidoRepositorio {
        private final Map<UUID, Pedido> dados = new HashMap<>();

        @Override
        public void salvar(Pedido pedido) {
            dados.put(pedido.getId(), pedido);
        }

        @Override
        public Optional<Pedido> buscar(UUID id) {
            return Optional.ofNullable(dados.get(id));
        }

        @Override
        public Pedido obter(UUID id) {
            Pedido p = dados.get(id);
            if (p == null) {
                throw new IllegalArgumentException("Pedido não encontrado: " + id);
            }
            return p;
        }

        @Override
        public void remover(UUID id) {
            dados.remove(id);
        }
    }
}
