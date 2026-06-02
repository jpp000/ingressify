package cesar.rv.ingressify.dominio.padroes.proxy;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface UsuarioServico {

	boolean podeCriarAnuncioRevenda(UsuarioId usuarioId);

	boolean podeComprarIngresso(UsuarioId usuarioId);
}
