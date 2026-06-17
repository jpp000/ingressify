package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.FilaEsperaJpa;

public interface FilaEsperaSpringDataRepository extends JpaRepository<FilaEsperaJpa, Integer> {

    Optional<FilaEsperaJpa> findByMapaIdAndUsuarioId(Integer mapaId, Integer usuarioId);

    List<FilaEsperaJpa> findByMapaIdOrderByPosicaoAsc(Integer mapaId);

    void deleteByMapaIdAndUsuarioId(Integer mapaId, Integer usuarioId);

    int countByMapaId(Integer mapaId);
}
