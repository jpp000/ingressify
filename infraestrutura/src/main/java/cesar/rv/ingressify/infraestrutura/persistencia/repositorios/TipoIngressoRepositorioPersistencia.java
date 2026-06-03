package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.Lote;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.LoteId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.LoteJpa;
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
		if (!tipoIngresso.getLotes().isEmpty()) {
			Map<Integer, Integer> idPorNumero = saved.getLotes().stream()
					.collect(Collectors.toMap(LoteJpa::getNumero, LoteJpa::getId));
			for (Lote lote : tipoIngresso.getLotes()) {
				if (lote.getId() == null) {
					Integer loteId = idPorNumero.get(lote.getNumero());
					if (loteId != null) lote.atribuirId(new LoteId(loteId));
				}
			}
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
