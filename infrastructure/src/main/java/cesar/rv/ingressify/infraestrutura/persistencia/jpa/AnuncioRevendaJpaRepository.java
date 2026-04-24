package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cesar.rv.ingressify.aplicacao.marketplace.anuncio.AnuncioResumo;

public interface AnuncioRevendaJpaRepository extends JpaRepository<AnuncioRevendaJpa, Integer> {

	@Query("select a.id as id, a.ingressoId as ingressoId, a.vendedorId as vendedorId, a.preco.valor as precoValor, a.compradorReservadoId as compradorReservadoId, a.status as status from AnuncioRevendaJpa a")
	List<AnuncioResumo> pesquisarResumos();

	@Query("select a.id as id, a.ingressoId as ingressoId, a.vendedorId as vendedorId, a.preco.valor as precoValor, a.compradorReservadoId as compradorReservadoId, a.status as status from AnuncioRevendaJpa a where a.id = :id")
	AnuncioResumo obterResumo(@Param("id") int id);

	@Query("select a from AnuncioRevendaJpa a where a.correlacaoPagamento = :c")
	Optional<AnuncioRevendaJpa> findByCorrelacaoPagamento(@Param("c") UUID correlacao);

	@Query("select case when count(a) > 0 then true else false end from AnuncioRevendaJpa a where a.ingressoId = :ing and a.status in ('DISPONIVEL','RESERVADO')")
	boolean existsAtivoParaIngresso(@Param("ing") UUID ing);
}
