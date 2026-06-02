package cesar.rv.ingressify.dominio.padroes.proxy;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class UsuarioBloqueadoParaRevendaException extends RuntimeException {

	public UsuarioBloqueadoParaRevendaException(UsuarioId usuarioId) {
		super("usuário bloqueado para revenda: " + usuarioId);
	}
}
