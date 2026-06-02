package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.EventoJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.EventoSpringDataRepository;

@Repository
public class EventoRepositorioPersistencia implements EventoRepositorio {

	private final EventoSpringDataRepository jpa;

	public EventoRepositorioPersistencia(EventoSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(Evento evento) {
		EventoJpa saved = jpa.save(EventoJpa.fromDomain(evento));
		if (evento.getId() == null) {
			evento.atribuirId(new EventoId(saved.getId()));
		}
	}

	@Override
	public Evento obter(EventoId id) {
		return jpa.findById(id.getId())
				.map(EventoJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("Evento não encontrado: " + id));
	}

	@Override
	public void remover(EventoId id) {
		jpa.deleteById(id.getId());
	}

	@Override
	public List<Evento> listarAtivos() {
		return jpa.findByStatus(StatusEvento.ATIVO).stream()
				.map(EventoJpa::toDomain)
				.toList();
	}

	@Override
	public List<Evento> listarPorOrganizador(UsuarioId organizadorId) {
		return jpa.findByOrganizadorId(organizadorId.getId()).stream()
				.map(EventoJpa::toDomain)
				.toList();
	}

	@Override
	public List<Evento> pesquisarPorIds(Collection<EventoId> ids) {
		Iterable<Integer> intIds = ids.stream().map(EventoId::getId)::iterator;
		return StreamSupport.stream(jpa.findByIdIn(intIds).spliterator(), false)
				.map(EventoJpa::toDomain)
				.toList();
	}
}
