package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.feed.Postagem;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemId;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemRepositorio;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.PostagemJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.PostagemSpringDataRepository;

@Repository
public class PostagemRepositorioPersistencia implements PostagemRepositorio {

	private final PostagemSpringDataRepository jpa;

	public PostagemRepositorioPersistencia(PostagemSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(Postagem postagem) {
		PostagemJpa saved = jpa.save(PostagemJpa.fromDomain(postagem));
		if (postagem.getId() == null) {
			postagem.atribuirId(new PostagemId(saved.getId()));
		}
	}

	@Override
	public Postagem obter(PostagemId id) {
		return jpa.findById(id.getId())
				.map(PostagemJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("Postagem não encontrada: " + id.getId()));
	}

	@Override
	public void remover(PostagemId id) {
		jpa.deleteById(id.getId());
	}

	@Override
	public List<Postagem> pesquisarPorEvento(EventoId eventoId) {
		return jpa.findByEventoIdOrderByCriadaEmDesc(eventoId.getId()).stream()
				.map(PostagemJpa::toDomain)
				.toList();
	}

	@Override
	public Optional<Postagem> buscarFixadaPorEvento(EventoId eventoId) {
		return jpa.findByEventoIdAndFixadaTrue(eventoId.getId())
				.map(PostagemJpa::toDomain);
	}
}
