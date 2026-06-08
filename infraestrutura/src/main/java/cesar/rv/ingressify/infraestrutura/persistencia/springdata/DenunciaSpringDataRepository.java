package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.dominio.marketplace.denuncia.StatusDenuncia;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.DenunciaJpa;

public interface DenunciaSpringDataRepository extends JpaRepository<DenunciaJpa, Integer> {

	boolean existsByAnuncioIdAndDenuncianteId(Integer anuncioId, Integer denuncianteId);

	List<DenunciaJpa> findByStatus(StatusDenuncia status);
}
