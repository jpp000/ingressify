package cesar.rv.ingressify.dominio.marketplace.mapaAssentos;

import java.util.List;
import java.util.Optional;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface MapaAssentosRepositorio {

    void salvar(MapaAssentos mapa);

    MapaAssentos obter(MapaAssentosId id);

    Optional<MapaAssentos> buscarPorEvento(EventoId eventoId);

    void salvarAssento(Assento assento);

    Assento obterAssento(AssentoId id);

    List<Assento> listarAssentosPorMapa(MapaAssentosId mapaId);

    List<Assento> listarAssentosPorEvento(EventoId eventoId);
}
