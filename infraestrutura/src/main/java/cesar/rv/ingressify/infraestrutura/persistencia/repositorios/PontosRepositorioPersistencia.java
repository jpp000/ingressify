package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.financeiro.pontos.Pontos;
import cesar.rv.ingressify.dominio.financeiro.pontos.PontosRepositorio;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.PontosJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.PontosSpringDataRepository;

@Repository
public class PontosRepositorioPersistencia implements PontosRepositorio {

	private final PontosSpringDataRepository jpa;

	public PontosRepositorioPersistencia(PontosSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(Pontos pontos) {
		jpa.save(PontosJpa.fromDomain(pontos));
	}

	@Override
	public Pontos obter(UsuarioId usuario) {
		return jpa.findByUsuarioId(usuario.getId())
				.map(PontosJpa::toDomain)
				.orElseGet(() -> {
					Pontos novo = new Pontos(usuario, 0);
					salvar(novo);
					return novo;
				});
	}
}
