package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.marketplace.cupom.Cupom;
import cesar.rv.ingressify.dominio.marketplace.cupom.CupomId;
import cesar.rv.ingressify.dominio.marketplace.cupom.CupomRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.CupomJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.CupomSpringDataRepository;

@Repository
public class CupomRepositorioPersistencia implements CupomRepositorio {

	private final CupomSpringDataRepository jpa;

	public CupomRepositorioPersistencia(CupomSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(Cupom cupom) {
		CupomJpa saved = jpa.save(CupomJpa.fromDomain(cupom));
		if (cupom.getId() == null) {
			cupom.atribuirId(new CupomId(saved.getId()));
		}
	}

	@Override
	public Cupom obter(CupomId id) {
		return jpa.findById(id.getId())
				.map(CupomJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("Cupom não encontrado: " + id));
	}

	@Override
	public Optional<Cupom> buscarPorCodigo(String codigo) {
		return jpa.findByCodigo(codigo).map(CupomJpa::toDomain);
	}

	@Override
	public List<Cupom> pesquisarPorEvento(EventoId eventoId) {
		return jpa.findByEventoId(eventoId.getId()).stream()
				.map(CupomJpa::toDomain).toList();
	}
}
