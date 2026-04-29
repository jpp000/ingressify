package cesar.rv.ingressify.aplicacao.identidade.usuario;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface UsuarioResumo {

	UsuarioId getId();

	String getNome();

	String getEmail();

	String getCidade();

	boolean isBloqueadoRevenda();
}
