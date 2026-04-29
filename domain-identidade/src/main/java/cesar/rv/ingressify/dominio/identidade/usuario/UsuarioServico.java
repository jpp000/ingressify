package cesar.rv.ingressify.dominio.identidade.usuario;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class UsuarioServico {

	private final UsuarioRepositorio repositorio;

	public UsuarioServico(UsuarioRepositorio repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public UsuarioId cadastrar(String nome, String email, String senhaPlana) {
		Validate.notBlank(nome, "nome");
		Validate.notBlank(email, "email");
		Validate.notBlank(senhaPlana, "senhaPlana");
		if (repositorio.existeEmail(email.trim().toLowerCase())) {
			throw new IllegalStateException("e-mail já cadastrado");
		}
		PoliticaSenha.validar(senhaPlana);
		Usuario u = new Usuario(nome, email.trim().toLowerCase(), PasswordHasher.hash(senhaPlana));
		repositorio.salvar(u);
		return u.getId();
	}

	public void atualizarPerfil(UsuarioId usuarioId, String nome, String fotoUrl, String cidade,
			java.time.LocalDate dataNascimento) {
		Usuario u = repositorio.obter(usuarioId);
		u.atualizarPerfil(nome, fotoUrl, cidade, dataNascimento);
		repositorio.salvar(u);
	}

	public void alterarEmail(UsuarioId usuarioId, String novoEmail, String senhaAtual) {
		Usuario u = repositorio.obter(usuarioId);
		u.alterarEmail(novoEmail.trim().toLowerCase(), senhaAtual);
		repositorio.salvar(u);
	}

	public void alterarSenha(UsuarioId usuarioId, String atual, String nova) {
		Usuario u = repositorio.obter(usuarioId);
		u.alterarSenha(atual, nova);
		repositorio.salvar(u);
	}

	public void bloquearRevenda(UsuarioId usuarioId) {
		Usuario u = repositorio.obter(usuarioId);
		u.bloquearRevenda();
		repositorio.salvar(u);
	}

	public void desbloquearRevenda(UsuarioId usuarioId) {
		Usuario u = repositorio.obter(usuarioId);
		u.desbloquearRevenda();
		repositorio.salvar(u);
	}

	public void concederPapel(UsuarioId usuarioId, Papel papel) {
		Usuario u = repositorio.obter(usuarioId);
		u.concederPapel(papel);
		repositorio.salvar(u);
	}

	public void revogarPapel(UsuarioId usuarioId, Papel papel) {
		Usuario u = repositorio.obter(usuarioId);
		u.revogarPapel(papel);
		repositorio.salvar(u);
	}

	public Usuario obter(UsuarioId id) {
		return repositorio.obter(id);
	}

	public Usuario obterPorEmail(String email) {
		return repositorio.obterPorEmail(email.trim().toLowerCase());
	}

	public void salvar(Usuario usuario) {
		repositorio.salvar(usuario);
	}
}
