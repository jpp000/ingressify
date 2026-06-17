package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.PostagemJpa;

public interface PostagemSpringDataRepository extends JpaRepository<PostagemJpa, Integer> {

	List<PostagemJpa> findByEventoIdOrderByCriadaEmDesc(Integer eventoId);

	Optional<PostagemJpa> findByEventoIdAndFixadaTrue(Integer eventoId);
}
