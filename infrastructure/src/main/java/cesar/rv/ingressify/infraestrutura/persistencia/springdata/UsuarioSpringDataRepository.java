package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.UsuarioJpa;

public interface UsuarioSpringDataRepository extends JpaRepository<UsuarioJpa, Integer> {

	Optional<UsuarioJpa> findByEmail(String email);

	boolean existsByEmail(String email);
}
