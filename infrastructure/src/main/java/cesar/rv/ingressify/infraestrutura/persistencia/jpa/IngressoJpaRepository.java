package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cesar.rv.ingressify.aplicacao.marketplace.ingresso.IngressoResumo;

public interface IngressoJpaRepository extends JpaRepository<IngressoJpa, UUID> {

	List<IngressoJpa> findByProprietarioId(int proprietarioId);

	@Query("select i.id as id, i.tipoIngressoId as tipoIngressoId, i.eventoId as eventoId, i.proprietarioId as proprietarioId, i.status as status from IngressoJpa i")
	List<IngressoResumo> pesquisarResumos();

	@Query("select i.id as id, i.tipoIngressoId as tipoIngressoId, i.eventoId as eventoId, i.proprietarioId as proprietarioId, i.status as status from IngressoJpa i where i.proprietarioId = :uid")
	List<IngressoResumo> pesquisarResumosPorProprietario(@Param("uid") int uid);

	@Query("select i.id as id, i.tipoIngressoId as tipoIngressoId, i.eventoId as eventoId, i.proprietarioId as proprietarioId, i.status as status from IngressoJpa i where i.id = :id")
	IngressoResumo obterResumo(@Param("id") UUID id);
}
