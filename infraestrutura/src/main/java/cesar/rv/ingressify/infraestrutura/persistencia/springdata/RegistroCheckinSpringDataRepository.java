package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.RegistroCheckinJpa;

public interface RegistroCheckinSpringDataRepository extends JpaRepository<RegistroCheckinJpa, UUID> {

	List<RegistroCheckinJpa> findByEventoId(Integer eventoId);
}
