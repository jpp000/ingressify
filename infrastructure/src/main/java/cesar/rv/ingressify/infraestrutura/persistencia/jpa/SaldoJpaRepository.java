package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cesar.rv.ingressify.aplicacao.financeiro.saldo.SaldoResumo;

public interface SaldoJpaRepository extends JpaRepository<SaldoJpa, Integer> {

	@Query("select s.usuarioId as usuarioId, s.valor.valor as valor from SaldoJpa s where s.usuarioId = :uid")
	SaldoResumo obterResumo(@Param("uid") int uid);
}
