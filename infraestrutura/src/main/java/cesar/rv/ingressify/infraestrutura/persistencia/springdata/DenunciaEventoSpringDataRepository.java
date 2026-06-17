package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.dominio.marketplace.denuncia.StatusDenunciaEvento;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.DenunciaEventoJpa;

public interface DenunciaEventoSpringDataRepository extends JpaRepository<DenunciaEventoJpa, Integer> {

	List<DenunciaEventoJpa> findAllByOrderByCriadaEmDesc();

	List<DenunciaEventoJpa> findByEventoId(Integer eventoId);

	boolean existsByEventoIdAndDenuncianteId(Integer eventoId, Integer denuncianteId);

	List<DenunciaEventoJpa> findByStatus(StatusDenunciaEvento status);
}
