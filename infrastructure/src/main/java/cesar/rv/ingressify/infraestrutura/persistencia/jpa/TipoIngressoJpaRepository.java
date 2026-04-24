package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso.TipoIngressoResumo;

public interface TipoIngressoJpaRepository extends JpaRepository<TipoIngressoJpa, Integer> {

	List<TipoIngressoJpa> findByEventoId(int eventoId);

	@Query("select t.id as id, t.eventoId as eventoId, t.nome as nome, t.preco.valor as precoValor, t.quantidadeDisponivel as quantidadeDisponivel, t.quantidadeTotal as quantidadeTotal from TipoIngressoJpa t where t.eventoId = :eventoId")
	List<TipoIngressoResumo> pesquisarResumosPorEvento(@Param("eventoId") int eventoId);

	@Query("select t.id as id, t.eventoId as eventoId, t.nome as nome, t.preco.valor as precoValor, t.quantidadeDisponivel as quantidadeDisponivel, t.quantidadeTotal as quantidadeTotal from TipoIngressoJpa t")
	List<TipoIngressoResumo> pesquisarResumos();

	@Query("select t.id as id, t.eventoId as eventoId, t.nome as nome, t.preco.valor as precoValor, t.quantidadeDisponivel as quantidadeDisponivel, t.quantidadeTotal as quantidadeTotal from TipoIngressoJpa t where t.id = :id")
	TipoIngressoResumo obterResumo(@Param("id") int id);
}
