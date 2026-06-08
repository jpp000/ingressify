package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.Avaliacao;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoId;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.AvaliacaoJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.AvaliacaoSpringDataRepository;

@Repository
public class AvaliacaoRepositorioPersistencia implements AvaliacaoRepositorio {

	private final AvaliacaoSpringDataRepository jpa;

	public AvaliacaoRepositorioPersistencia(AvaliacaoSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(Avaliacao avaliacao) {
		AvaliacaoJpa saved = jpa.save(AvaliacaoJpa.fromDomain(avaliacao));
		if (avaliacao.getId() == null) {
			avaliacao.atribuirId(new AvaliacaoId(saved.getId()));
		}
	}

	@Override
	public Avaliacao obter(AvaliacaoId id) {
		return jpa.findById(id.getId())
				.map(AvaliacaoJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("Avaliacao não encontrada: " + id.getId()));
	}

	@Override
	public Optional<Avaliacao> buscarPorEventoEUsuario(EventoId eventoId, UsuarioId usuarioId) {
		return jpa.findByEventoIdAndUsuarioId(eventoId.getId(), usuarioId.getId())
				.map(AvaliacaoJpa::toDomain);
	}

	@Override
	public List<Avaliacao> pesquisarPorEvento(EventoId eventoId) {
		return jpa.findByEventoId(eventoId.getId()).stream()
				.map(AvaliacaoJpa::toDomain)
				.toList();
	}
}
