package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.dominio.marketplace.grupoCompra.StatusGrupoCompra;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.GrupoCompraJpa;

public interface GrupoCompraSpringDataRepository extends JpaRepository<GrupoCompraJpa, Integer> {

    List<GrupoCompraJpa> findByEventoId(Integer eventoId);

    List<GrupoCompraJpa> findByLiderId(Integer liderId);

    List<GrupoCompraJpa> findByStatusAndPrazoPagamentoBefore(StatusGrupoCompra status, LocalDateTime momento);
}
