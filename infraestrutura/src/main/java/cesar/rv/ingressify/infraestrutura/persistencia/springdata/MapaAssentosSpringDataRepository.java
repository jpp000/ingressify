package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.infraestrutura.persistencia.jpa.MapaAssentosJpa;

public interface MapaAssentosSpringDataRepository extends JpaRepository<MapaAssentosJpa, Integer> {

    Optional<MapaAssentosJpa> findByEventoId(Integer eventoId);
}
