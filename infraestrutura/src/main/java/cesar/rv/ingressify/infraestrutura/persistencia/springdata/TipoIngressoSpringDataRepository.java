package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.TipoIngressoJpa;

public interface TipoIngressoSpringDataRepository extends JpaRepository<TipoIngressoJpa, Integer> {

	List<TipoIngressoJpa> findByEventoId(Integer eventoId);
}
