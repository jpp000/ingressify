package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cesar.rv.ingressify.aplicacao.financeiro.transacao.TransacaoResumo;

public interface TransacaoJpaRepository extends JpaRepository<TransacaoJpa, Integer> {

	List<TransacaoJpa> findByUsuarioIdOrderByDataDesc(int usuarioId);

	@Query("select t.id as id, t.usuarioId as usuarioId, t.tipo as tipo, t.valor.valor as valor, t.data as data, t.referenciaExternaId as referenciaExternaId from TransacaoJpa t where t.usuarioId = :uid order by t.data desc")
	List<TransacaoResumo> pesquisarHistorico(@Param("uid") int uid);
}
