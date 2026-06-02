package cesar.rv.ingressify.dominio.padroes.proxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

class UsuarioServicoProxyTest {

	private static final UsuarioId USUARIO_BLOQUEADO = new UsuarioId(1);
	private static final UsuarioId USUARIO_LIVRE = new UsuarioId(2);

	private UsuarioServico delegado;
	private VerificadorBloqueioRevenda verificador;

	@BeforeEach
	void setUp() {
		delegado = new UsuarioServico() {
			@Override
			public boolean podeCriarAnuncioRevenda(UsuarioId usuarioId) {
				return true;
			}

			@Override
			public boolean podeComprarIngresso(UsuarioId usuarioId) {
				return true;
			}
		};
		verificador = uid -> uid.equals(USUARIO_BLOQUEADO);
	}

	@Test
	void usuarioBloqueadoNaoPodeCriarAnuncio() {
		var proxy = new UsuarioServicoProxy(delegado, verificador);

		assertThrows(UsuarioBloqueadoParaRevendaException.class,
				() -> proxy.podeCriarAnuncioRevenda(USUARIO_BLOQUEADO));
	}

	@Test
	void usuarioNaoBloqueadoPodeCriarAnuncio() {
		var proxy = new UsuarioServicoProxy(delegado, verificador);

		assertDoesNotThrow(() -> proxy.podeCriarAnuncioRevenda(USUARIO_LIVRE));
		assertTrue(proxy.podeCriarAnuncioRevenda(USUARIO_LIVRE));
	}

	@Test
	void usuarioBloqueadoAindaPodeComprarIngresso() {
		var proxy = new UsuarioServicoProxy(delegado, verificador);

		assertDoesNotThrow(() -> proxy.podeComprarIngresso(USUARIO_BLOQUEADO));
		assertTrue(proxy.podeComprarIngresso(USUARIO_BLOQUEADO));
	}

	@Test
	void excecaoContemIdDoUsuarioBloqueado() {
		var proxy = new UsuarioServicoProxy(delegado, verificador);

		var ex = assertThrows(UsuarioBloqueadoParaRevendaException.class,
				() -> proxy.podeCriarAnuncioRevenda(USUARIO_BLOQUEADO));
		assertTrue(ex.getMessage().contains(String.valueOf(USUARIO_BLOQUEADO.getId())));
	}

	@Test
	void todosBloqueadosQuandoVerificadorSempreRetornaTrue() {
		var proxy = new UsuarioServicoProxy(delegado, uid -> true);

		assertThrows(UsuarioBloqueadoParaRevendaException.class,
				() -> proxy.podeCriarAnuncioRevenda(USUARIO_LIVRE));
	}
}
