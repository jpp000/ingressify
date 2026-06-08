package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.AnuncioRevendaJpa;

public interface AnuncioRevendaSpringDataRepository extends JpaRepository<AnuncioRevendaJpa, Integer> {

	List<AnuncioRevendaJpa> findByEventoId(Integer eventoId);

	List<AnuncioRevendaJpa> findByVendedorId(Integer vendedorId);

	@Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END FROM AnuncioRevendaJpa a JOIN a.ingressoIds i WHERE i = :ingressoId AND a.status IN :statuses")
	boolean existeAtivoPorIngresso(@Param("ingressoId") UUID ingressoId, @Param("statuses") Collection<StatusAnuncio> statuses);

	@Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END FROM AnuncioRevendaJpa a WHERE a.eventoId = :eventoId AND a.status IN :statuses")
	boolean existeAtivoPorEvento(@Param("eventoId") Integer eventoId, @Param("statuses") Collection<StatusAnuncio> statuses);
}
