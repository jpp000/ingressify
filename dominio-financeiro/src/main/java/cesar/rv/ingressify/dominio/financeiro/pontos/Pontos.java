package cesar.rv.ingressify.dominio.financeiro.pontos;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

/**
 * Pontos de fidelidade acumulados pelo usuário. São creditados a cada recarga
 * da carteira e podem ser resgatados em saldo (ver CarteiraServicoAplicacao).
 */
public class Pontos {

	private UsuarioId usuario;
	private int quantidade;

	public Pontos(UsuarioId usuario, int quantidade) {
		Validate.notNull(usuario, "usuario");
		Validate.isTrue(quantidade >= 0, "quantidade deve ser >= 0");
		this.usuario = usuario;
		this.quantidade = quantidade;
	}

	public void adicionar(int pontos) {
		Validate.isTrue(pontos >= 0, "pontos deve ser >= 0");
		this.quantidade += pontos;
	}

	public void remover(int pontos) {
		Validate.isTrue(pontos >= 0, "pontos deve ser >= 0");
		if (pontos > quantidade) {
			throw new IllegalStateException("pontos insuficientes");
		}
		this.quantidade -= pontos;
	}

	public UsuarioId getUsuario() {
		return usuario;
	}

	public int getQuantidade() {
		return quantidade;
	}
}
