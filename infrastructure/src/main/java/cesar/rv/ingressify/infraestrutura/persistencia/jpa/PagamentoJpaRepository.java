package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cesar.rv.ingressify.aplicacao.financeiro.pagamento.PagamentoResumo;

public interface PagamentoJpaRepository extends JpaRepository<PagamentoJpa, Integer> {

	@Query("select p.id as id, p.valor.valor as valor, p.compradorId as compradorId, p.vendedorId as vendedorId, p.tipo as tipo, p.status as status, p.correlacaoId as correlacaoId from PagamentoJpa p")
	List<PagamentoResumo> pesquisarResumos();

	@Query("select p.id as id, p.valor.valor as valor, p.compradorId as compradorId, p.vendedorId as vendedorId, p.tipo as tipo, p.status as status, p.correlacaoId as correlacaoId from PagamentoJpa p where p.id = :id")
	PagamentoResumo obterResumo(@Param("id") int id);

	Optional<PagamentoJpa> findByCorrelacaoId(UUID correlacaoId);
}
