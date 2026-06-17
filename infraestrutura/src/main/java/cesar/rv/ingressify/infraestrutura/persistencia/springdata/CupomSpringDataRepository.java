package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.CupomJpa;

public interface CupomSpringDataRepository extends JpaRepository<CupomJpa, Integer> {

	Optional<CupomJpa> findByCodigo(String codigo);

	List<CupomJpa> findByEventoId(Integer eventoId);
}
