package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.ParticipanteGrupoJpa;

public interface ParticipanteGrupoSpringDataRepository extends JpaRepository<ParticipanteGrupoJpa, Integer> {

    List<ParticipanteGrupoJpa> findByGrupoCompraId(Integer grupoCompraId);

    Optional<ParticipanteGrupoJpa> findByGrupoCompraIdAndUsuarioId(Integer grupoCompraId, Integer usuarioId);

    List<ParticipanteGrupoJpa> findByUsuarioId(Integer usuarioId);
}
