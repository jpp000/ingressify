package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraPendenteJpaRepository extends JpaRepository<CompraPendenteJpa, UUID> {
}
