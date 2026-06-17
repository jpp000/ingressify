package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.PedidoJpa;

public interface PedidoSpringDataRepository extends JpaRepository<PedidoJpa, UUID> {

	List<PedidoJpa> findByCompradorIdAndTipoIngressoIdOrderByCriadaEmDesc(Integer compradorId, Integer tipoIngressoId);
}
