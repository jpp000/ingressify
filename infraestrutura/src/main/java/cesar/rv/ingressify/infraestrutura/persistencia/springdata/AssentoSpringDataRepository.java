package cesar.rv.ingressify.infraestrutura.persistencia.springdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.StatusAssento;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.AssentoJpa;

public interface AssentoSpringDataRepository extends JpaRepository<AssentoJpa, Integer> {

    List<AssentoJpa> findByMapaId(Integer mapaId);

    List<AssentoJpa> findByEventoId(Integer eventoId);

    List<AssentoJpa> findByReservadoPorAndStatus(Integer reservadoPor, StatusAssento status);
}
