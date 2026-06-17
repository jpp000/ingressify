package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.TransacaoJpa;

public interface TransacaoSpringDataRepository extends JpaRepository<TransacaoJpa, Integer> {

	List<TransacaoJpa> findByUsuarioId(Integer usuarioId);

	List<TransacaoJpa> findByUsuarioIdOrderByDataDesc(Integer usuarioId);
}
