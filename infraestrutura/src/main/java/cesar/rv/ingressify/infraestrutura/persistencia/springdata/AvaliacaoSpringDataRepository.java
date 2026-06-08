package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.AvaliacaoJpa;

public interface AvaliacaoSpringDataRepository extends JpaRepository<AvaliacaoJpa, Integer> {

	List<AvaliacaoJpa> findByEventoId(Integer eventoId);

	Optional<AvaliacaoJpa> findByEventoIdAndUsuarioId(Integer eventoId, Integer usuarioId);
}
