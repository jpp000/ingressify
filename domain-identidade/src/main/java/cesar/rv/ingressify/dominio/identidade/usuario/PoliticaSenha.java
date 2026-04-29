package cesar.rv.ingressify.dominio.identidade.usuario;

import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;

public final class PoliticaSenha {

	private static final Pattern PADRAO = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");

	private PoliticaSenha() {
	}

	public static void validar(String senhaPlana) {
		Validate.notBlank(senhaPlana, "senha");
		if (!PADRAO.matcher(senhaPlana).matches()) {
			throw new IllegalArgumentException(
					"senha deve ter no mínimo 8 caracteres, ao menos uma maiúscula e um número");
		}
	}
}
