package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.FilaEspera;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.FilaEsperaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.FilaEsperaJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.FilaEsperaSpringDataRepository;

@Repository
public class FilaEsperaRepositorioPersistencia implements FilaEsperaRepositorio {

    private final FilaEsperaSpringDataRepository springData;

    public FilaEsperaRepositorioPersistencia(FilaEsperaSpringDataRepository springData) {
        this.springData = springData;
    }

    @Override
    public void salvar(FilaEspera entrada) {
        FilaEsperaJpa jpa = FilaEsperaJpa.fromDomain(entrada);
        FilaEsperaJpa salvo = springData.save(jpa);
        if (entrada.getId() == null) entrada.atribuirId(salvo.getId());
    }

    @Override
    @Transactional
    public void remover(MapaAssentosId mapaId, UsuarioId usuarioId) {
        springData.deleteByMapaIdAndUsuarioId(mapaId.getId(), usuarioId.getId());
    }

    @Override
    public Optional<FilaEspera> buscar(MapaAssentosId mapaId, UsuarioId usuarioId) {
        return springData.findByMapaIdAndUsuarioId(mapaId.getId(), usuarioId.getId())
                .map(FilaEsperaJpa::toDomain);
    }

    @Override
    public List<FilaEspera> listarPorMapa(MapaAssentosId mapaId) {
        return springData.findByMapaIdOrderByPosicaoAsc(mapaId.getId()).stream()
                .map(FilaEsperaJpa::toDomain)
                .toList();
    }

    @Override
    public Optional<FilaEspera> obterProximo(MapaAssentosId mapaId) {
        return springData.findByMapaIdOrderByPosicaoAsc(mapaId.getId())
                .stream().findFirst().map(FilaEsperaJpa::toDomain);
    }

    @Override
    public int contarPorMapa(MapaAssentosId mapaId) {
        return springData.countByMapaId(mapaId.getId());
    }
}
