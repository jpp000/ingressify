package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckin;
import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckinRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.RegistroCheckinJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.RegistroCheckinSpringDataRepository;

@Repository
public class RegistroCheckinRepositorioPersistencia implements RegistroCheckinRepositorio {

	private final RegistroCheckinSpringDataRepository jpa;

	public RegistroCheckinRepositorioPersistencia(RegistroCheckinSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(RegistroCheckin registro) {
		jpa.save(RegistroCheckinJpa.fromDomain(registro));
	}

	@Override
	public List<RegistroCheckin> pesquisarPorEvento(EventoId eventoId) {
		return jpa.findByEventoId(eventoId.getId()).stream()
				.map(RegistroCheckinJpa::toDomain)
				.toList();
	}
}
