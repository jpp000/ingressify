package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.Assento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.AssentoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentos;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosRepositorio;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.AssentoJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.MapaAssentosJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.AssentoSpringDataRepository;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.MapaAssentosSpringDataRepository;

@Repository
public class MapaAssentosRepositorioPersistencia implements MapaAssentosRepositorio {

    private final MapaAssentosSpringDataRepository mapaSpringData;
    private final AssentoSpringDataRepository assentoSpringData;

    public MapaAssentosRepositorioPersistencia(MapaAssentosSpringDataRepository mapaSpringData,
            AssentoSpringDataRepository assentoSpringData) {
        this.mapaSpringData = mapaSpringData;
        this.assentoSpringData = assentoSpringData;
    }

    @Override
    public void salvar(MapaAssentos mapa) {
        MapaAssentosJpa jpa = MapaAssentosJpa.fromDomain(mapa);
        MapaAssentosJpa salvo = mapaSpringData.save(jpa);
        if (mapa.getId() == null) {
            mapa.atribuirId(new MapaAssentosId(salvo.getId()));
        }
    }

    @Override
    public MapaAssentos obter(MapaAssentosId id) {
        return mapaSpringData.findById(id.getId())
                .map(MapaAssentosJpa::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Mapa de assentos não encontrado: " + id));
    }

    @Override
    public Optional<MapaAssentos> buscarPorEvento(EventoId eventoId) {
        return mapaSpringData.findByEventoId(eventoId.getId())
                .map(MapaAssentosJpa::toDomain);
    }

    @Override
    public void salvarAssento(Assento assento) {
        AssentoJpa jpa = AssentoJpa.fromDomain(assento);
        AssentoJpa salvo = assentoSpringData.save(jpa);
        if (assento.getId() == null) {
            assento.atribuirId(new AssentoId(salvo.getId()));
        }
    }

    @Override
    public Assento obterAssento(AssentoId id) {
        return assentoSpringData.findById(id.getId())
                .map(AssentoJpa::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Assento não encontrado: " + id));
    }

    @Override
    public List<Assento> listarAssentosPorMapa(MapaAssentosId mapaId) {
        return assentoSpringData.findByMapaId(mapaId.getId()).stream()
                .map(AssentoJpa::toDomain)
                .toList();
    }

    @Override
    public List<Assento> listarAssentosPorEvento(EventoId eventoId) {
        return assentoSpringData.findByEventoId(eventoId.getId()).stream()
                .map(AssentoJpa::toDomain)
                .toList();
    }

    @Override
    public List<MapaAssentosId> listarTodosIds() {
        return mapaSpringData.findAll().stream()
                .map(jpa -> new MapaAssentosId(jpa.getId()))
                .toList();
    }
}
