package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.ComentarioJpa;

public interface ComentarioSpringDataRepository extends JpaRepository<ComentarioJpa, Integer> {

	List<ComentarioJpa> findByPostagemIdOrderByCriadaEmAsc(Integer postagemId);
}
