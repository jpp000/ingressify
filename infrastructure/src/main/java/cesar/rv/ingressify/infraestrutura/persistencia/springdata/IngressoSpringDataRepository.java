package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.IngressoJpa;

public interface IngressoSpringDataRepository extends JpaRepository<IngressoJpa, Integer> {

	List<IngressoJpa> findByProprietarioId(Integer proprietarioId);

	List<IngressoJpa> findByEventoId(Integer eventoId);

	List<IngressoJpa> findByTipoIngressoId(Integer tipoIngressoId);

	@Query("SELECT COUNT(i) FROM IngressoJpa i WHERE i.tipoIngressoId = :tipoId AND i.status != :cancelado AND i.status != :reembolsado")
	int contarVendidosPorTipo(Integer tipoId,
			StatusIngresso cancelado, StatusIngresso reembolsado);
}
