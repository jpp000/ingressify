package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoResumo;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;

public interface EventoJpaRepository extends JpaRepository<EventoJpa, Integer> {

	@Query("select e.id as id, e.nome as nome, e.dataHora as dataHora, e.local as local, e.status as status, e.capacidade as capacidade from EventoJpa e")
	List<EventoResumo> pesquisarResumos();

	@Query("select e.id as id, e.nome as nome, e.dataHora as dataHora, e.local as local, e.status as status, e.capacidade as capacidade from EventoJpa e where e.status = :st and e.dataHora > :agora")
	List<EventoResumo> pesquisarAtivosFuturos(@Param("st") StatusEvento status, @Param("agora") LocalDateTime agora);

	@Query("select e.id as id, e.nome as nome, e.dataHora as dataHora, e.local as local, e.status as status, e.capacidade as capacidade from EventoJpa e where e.id = :id")
	Optional<EventoResumo> resumoPorId(@Param("id") int id);
}
