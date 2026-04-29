package cesar.rv.ingressify.dominio.identidade.usuario;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface UsuarioRepositorio {

	Usuario obter(UsuarioId id);

	void salvar(Usuario usuario);

	Usuario obterPorEmail(String email);

	boolean existeEmail(String email);
}
