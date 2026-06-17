package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.SorteioJpa;

public interface SorteioSpringDataRepository extends JpaRepository<SorteioJpa, Integer> {

    List<SorteioJpa> findByEventoId(Integer eventoId);
}
