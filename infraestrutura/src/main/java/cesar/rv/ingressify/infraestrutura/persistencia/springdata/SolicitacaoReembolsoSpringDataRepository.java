package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.dominio.marketplace.reembolso.StatusSolicitacaoReembolso;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.SolicitacaoReembolsoJpa;

public interface SolicitacaoReembolsoSpringDataRepository extends JpaRepository<SolicitacaoReembolsoJpa, Integer> {

	Optional<SolicitacaoReembolsoJpa> findByIngressoIdAndStatusIn(UUID ingressoId, java.util.List<StatusSolicitacaoReembolso> statuses);

	List<SolicitacaoReembolsoJpa> findBySolicitanteId(Integer solicitanteId);

	List<SolicitacaoReembolsoJpa> findAllByOrderByCriadaEmDesc();
}
