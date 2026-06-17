package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.PontosJpa;

public interface PontosSpringDataRepository extends JpaRepository<PontosJpa, Integer> {

	Optional<PontosJpa> findByUsuarioId(Integer usuarioId);
}
