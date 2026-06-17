package cesar.rv.ingressify.dominio.padroes.proxy;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

@FunctionalInterface
public interface VerificadorBloqueioRevenda {

	boolean estaBloqueado(UsuarioId usuarioId);
}
