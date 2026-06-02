package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.Usuario;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioRepositorio;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.UsuarioJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.UsuarioSpringDataRepository;

@Repository
public class UsuarioRepositorioPersistencia implements UsuarioRepositorio {

	private final UsuarioSpringDataRepository jpa;

	public UsuarioRepositorioPersistencia(UsuarioSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(Usuario usuario) {
		jpa.save(UsuarioJpa.fromDomain(usuario));
	}

	@Override
	public Usuario obter(UsuarioId id) {
		return jpa.findById(id.getId())
				.map(UsuarioJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
	}

	@Override
	public Usuario obterPorEmail(String email) {
		return jpa.findByEmail(email)
				.map(UsuarioJpa::toDomain)
				.orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
	}

	@Override
	public boolean existeEmail(String email) {
		return jpa.existsByEmail(email);
	}
}
