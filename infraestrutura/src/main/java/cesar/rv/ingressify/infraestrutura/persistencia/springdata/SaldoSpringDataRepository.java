package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.SaldoJpa;

public interface SaldoSpringDataRepository extends JpaRepository<SaldoJpa, Integer> {

	Optional<SaldoJpa> findByUsuarioId(Integer usuarioId);
}
