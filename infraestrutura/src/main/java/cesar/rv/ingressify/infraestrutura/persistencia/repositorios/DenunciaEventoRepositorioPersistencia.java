package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEvento;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEventoId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.DenunciaEventoJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.DenunciaEventoSpringDataRepository;

@Repository
public class DenunciaEventoRepositorioPersistencia implements DenunciaEventoRepositorio {

	private final DenunciaEventoSpringDataRepository jpa;

	public DenunciaEventoRepositorioPersistencia(DenunciaEventoSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(DenunciaEvento denuncia) {
		DenunciaEventoJpa saved = jpa.save(DenunciaEventoJpa.fromDomain(denuncia));
		if (denuncia.getId() == null) {
			denuncia.atribuirId(new DenunciaEventoId(saved.getId()));
		}
	}

	@Override
	public DenunciaEvento obter(DenunciaEventoId id) {
		return jpa.findById(id.getId())
				.map(DenunciaEventoJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("DenunciaEvento não encontrada: " + id.getId()));
	}

	@Override
	public List<DenunciaEvento> pesquisarTodas() {
		return jpa.findAllByOrderByCriadaEmDesc().stream()
				.map(DenunciaEventoJpa::toDomain)
				.toList();
	}

	@Override
	public List<DenunciaEvento> pesquisarPorEvento(EventoId eventoId) {
		return jpa.findByEventoId(eventoId.getId()).stream()
				.map(DenunciaEventoJpa::toDomain)
				.toList();
	}

	@Override
	public boolean existePorEventoEDenunciante(EventoId eventoId, UsuarioId denuncianteId) {
		return jpa.existsByEventoIdAndDenuncianteId(eventoId.getId(), denuncianteId.getId());
	}
}
