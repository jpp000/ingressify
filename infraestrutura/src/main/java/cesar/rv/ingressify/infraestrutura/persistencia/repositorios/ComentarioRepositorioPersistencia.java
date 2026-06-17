package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.marketplace.feed.Comentario;
import cesar.rv.ingressify.dominio.marketplace.feed.ComentarioId;
import cesar.rv.ingressify.dominio.marketplace.feed.ComentarioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemId;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.ComentarioJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.ComentarioSpringDataRepository;

@Repository
public class ComentarioRepositorioPersistencia implements ComentarioRepositorio {

	private final ComentarioSpringDataRepository jpa;

	public ComentarioRepositorioPersistencia(ComentarioSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(Comentario comentario) {
		ComentarioJpa saved = jpa.save(ComentarioJpa.fromDomain(comentario));
		if (comentario.getId() == null) {
			comentario.atribuirId(new ComentarioId(saved.getId()));
		}
	}

	@Override
	public List<Comentario> pesquisarPorPostagem(PostagemId postagemId) {
		return jpa.findByPostagemIdOrderByCriadaEmAsc(postagemId.getId()).stream()
				.map(ComentarioJpa::toDomain)
				.toList();
	}
}
