package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.InscricaoSorteioJpa;

public interface InscricaoSorteioSpringDataRepository extends JpaRepository<InscricaoSorteioJpa, Integer> {

    List<InscricaoSorteioJpa> findBySorteioId(Integer sorteioId);

    Optional<InscricaoSorteioJpa> findBySorteioIdAndParticipanteId(Integer sorteioId, Integer participanteId);
}
