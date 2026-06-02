package cesar.rv.ingressify.dominio.padroes.proxy;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class UsuarioServicoProxy implements UsuarioServico {

	private final UsuarioServico delegado;
	private final VerificadorBloqueioRevenda verificador;

	public UsuarioServicoProxy(UsuarioServico delegado, VerificadorBloqueioRevenda verificador) {
		Validate.notNull(delegado, "delegado");
		Validate.notNull(verificador, "verificador");
		this.delegado = delegado;
		this.verificador = verificador;
	}

	@Override
	public boolean podeCriarAnuncioRevenda(UsuarioId usuarioId) {
		Validate.notNull(usuarioId, "usuarioId");
		if (verificador.estaBloqueado(usuarioId)) {
			throw new UsuarioBloqueadoParaRevendaException(usuarioId);
		}
		return delegado.podeCriarAnuncioRevenda(usuarioId);
	}

	@Override
	public boolean podeComprarIngresso(UsuarioId usuarioId) {
		Validate.notNull(usuarioId, "usuarioId");
		return delegado.podeComprarIngresso(usuarioId);
	}
}
