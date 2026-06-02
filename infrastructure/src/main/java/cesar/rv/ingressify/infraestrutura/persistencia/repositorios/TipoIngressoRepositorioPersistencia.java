package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.TipoIngressoJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.TipoIngressoSpringDataRepository;

@Repository
public class TipoIngressoRepositorioPersistencia implements TipoIngressoRepositorio {

	private final TipoIngressoSpringDataRepository jpa;

	public TipoIngressoRepositorioPersistencia(TipoIngressoSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(TipoIngresso tipoIngresso) {
		TipoIngressoJpa saved = jpa.save(TipoIngressoJpa.fromDomain(tipoIngresso));
		if (tipoIngresso.getId() == null) {
			tipoIngresso.atribuirId(new TipoIngressoId(saved.getId()));
		}
	}

	@Override
	public TipoIngresso obter(TipoIngressoId id) {
		return jpa.findById(id.getId())
				.map(TipoIngressoJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("TipoIngresso não encontrado: " + id));
	}

	@Override
	public void remover(TipoIngressoId id) {
		jpa.deleteById(id.getId());
	}

	@Override
	public List<TipoIngresso> pesquisarPorEvento(EventoId eventoId) {
		return jpa.findByEventoId(eventoId.getId()).stream()
				.map(TipoIngressoJpa::toDomain).toList();
	}
}
