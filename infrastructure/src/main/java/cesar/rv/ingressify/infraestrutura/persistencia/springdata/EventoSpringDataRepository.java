package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.EventoJpa;

public interface EventoSpringDataRepository extends JpaRepository<EventoJpa, Integer> {

	List<EventoJpa> findByOrganizadorId(Integer organizadorId);

	List<EventoJpa> findByStatus(StatusEvento status);

	List<EventoJpa> findByIdIn(Iterable<Integer> ids);
}
